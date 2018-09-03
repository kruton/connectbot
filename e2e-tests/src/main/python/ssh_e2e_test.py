from mobly import base_test
from mobly import test_runner
from mobly.controllers import android_device
from time import sleep
import docker
import logging
import os

class SshE2eTest(base_test.BaseTestClass):
    def __init__(self, configs=None):
        super().__init__(configs=configs)
        self.logger = logging.getLogger()
        self.logger.setLevel(logging.DEBUG)

    def setup_class(self):
        self.client = docker.from_env()
        self.logger.info('Docker version: {0}'.format(self.client.version()))
        self.build_image()
        self.start_container()
        self.wait_for_container_to_start()

        self.ads = self.register_controller(android_device)
        self.dut = self.ads[0]
        self.setup_port_forward()
        self.dut.load_snippet('mbs', 'org.connectbot.mobly.debug')

    def teardown_class(self):
        if self.container:
            self.logger.info('Logs from  the docker container {0}:'.format(self.container.id))
            for line in self.container.logs(stream=True):
                self.logger.info('docker: {0}'.format(line))
            self.container.stop()

    def build_image(self):
        try:
            self.image, _ = self.client.images.build(
                path=os.path.dirname(os.path.abspath(__file__)) + '/../resources/openssh-server',
                rm=True)
        except BuildError as e:
            self.logger.error('Cannot build container. Build log:')
            for line in e.build_log:
                self.logger.error('  %s', line)
            raise e

    def start_container(self):
        self.container = self.client.containers.run(
            self.image,
            auto_remove=True,
            detach=True,
            ports={'22/tcp': None})

    def wait_for_container_to_start(self):
        self.logger.info('Waiting for container {0} to start...'.format(self.container.id))

        self.container.reload()
        while not self.container.attrs['NetworkSettings']['Ports']:
            sleep(0.1)
            self.container.reload()

        self.ssh_server_port = self.container.attrs['NetworkSettings']['Ports']['22/tcp'][0]['HostPort']
        self.logger.info('Host port for SSH is {0}'.format(self.ssh_server_port))

    def setup_port_forward(self):
        self.android_port = int(self.dut.adb.reverse(args=['tcp:0', 'tcp:{0}'.format(self.ssh_server_port)]))
        self.logger.info('Android port is {0}'.format(self.android_port))

    def test_ssh(self):
        self.dut.mbs.makeDatabasePristine()
        self.dut.mbs.setRotationToAuto()
        self.dut.mbs.runSshTest('localhost', self.android_port)

if __name__ == '__main__':
    test_runner.main()
