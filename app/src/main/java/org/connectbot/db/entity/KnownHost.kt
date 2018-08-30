package org.connectbot.db.entity

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
		foreignKeys = [ForeignKey(entity = Host::class, parentColumns = ["id"], childColumns = ["hostId"])],
		indices = [Index("hostId")]
)
class KnownHost {
	@PrimaryKey(autoGenerate = true)
	var id: Long = 0

	@NonNull
	var hostId: Long? = null

	@NonNull
	var hostKeyAlgorithm: String? = null

	@NonNull
	var hostKey: ByteArray? = null
}
