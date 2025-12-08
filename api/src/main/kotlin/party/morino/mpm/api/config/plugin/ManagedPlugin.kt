/*
 * Written in 2023-2025 by Nikomaru <nikomaru@nikomaru.dev>
 *
 * To the extent possible under law, the author(s) have dedicated all copyright
and related and neighboring rights to this software to the public domain worldwide.
This software is distributed without any warranty.
 *
 * You should have received a copy of the CC0 Public Domain Dedication along with this software.
 * If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package party.morino.mpm.api.config.plugin

import kotlinx.serialization.Serializable

@Serializable
data class ManagedPlugin(
    val pluginInfo: PluginInfo,
    val mpmInfo: MpmInfo
)

@Serializable
data class PluginInfo(
    val name: String,
    val version: String,
    val description: String? = null,
    val main: String? = null,
    val author: String? = null,
    val website: String? = null
)

@Serializable
data class MpmInfo(
    val repository: RepositoryInfo,
    val version: VersionManagement,
    val download: MetadataDownloadInfo,
    val settings: PluginSettings,
    val history: List<HistoryEntry>,
    val versionPattern: String? = null,
    val fileNamePattern: String? = null,
    val fileNameTemplate: String? = null
)

@Serializable
data class VersionManagement(
    val current: VersionDetail,
    val latest: VersionDetail,
    val lastChecked: String
)

@Serializable
data class VersionDetail(
    val raw: String,
    val normalized: String
)

@Serializable
data class MetadataDownloadInfo(
    val downloadId: String,
    val fileName: String? = null,
    val url: String? = null,
    val sha256: String? = null
)

@Serializable
data class HistoryEntry(
    val version: String,
    val installedAt: String,
    val action: String
)
