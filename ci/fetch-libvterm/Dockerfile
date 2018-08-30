FROM alpine:latest
WORKDIR /work
RUN apk add --no-cache bzr make perl sed
RUN bzr checkout --lightweight lp:libvterm
WORKDIR /work/libvterm
RUN make dist+bzr
