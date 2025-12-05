/*
 * Written in 2023-2025 by Nikomaru <nikomaru@nikomaru.dev>
 *
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide.This software is distributed without any warranty.
 *
 * You should have received a copy of the CC0 Public Domain Dedication along with this software.
 * If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package party.morino.mpm.core.plugin

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.charleskorn.kaml.Yaml
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.mpm.api.config.PluginDirectory
import party.morino.mpm.api.config.plugin.ManagedPlugin
import party.morino.mpm.api.core.plugin.DownloaderRepository
import party.morino.mpm.api.core.plugin.PluginInstallUseCase
import party.morino.mpm.api.model.repository.UrlData
import party.morino.mpm.api.model.repository.VersionData
import party.morino.mpm.api.utils.DataClassReplacer.replaceTemplate
import java.io.File

class PluginInstallUseCaseImpl :
    PluginInstallUseCase,
    KoinComponent {
    private val pluginDirectory: PluginDirectory by inject()
    private val downloaderRepository: DownloaderRepository by inject()

    override suspend fun installPlugin(pluginName: String): Either<String, Unit> {
        val metadataDir = pluginDirectory.getMetadataDirectory()
        val metadataFile = File(metadataDir, "$pluginName.yaml")

        if (!metadataFile.exists()) {
            return "メタデータファイルが見つかりません: $pluginName.yaml".left()
        }

        val metadata =
            try {
                val yamlString = metadataFile.readText()
                Yaml.default.decodeFromString(ManagedPlugin.serializer(), yamlString)
            } catch (e: Exception) {
                return "メタデータの読み込みに失敗しました: ${e.message}".left()
            }

        val mpmInfo = metadata.mpmInfo
        val pluginInfo = metadata.pluginInfo
        val repositoryInfo = mpmInfo.repository

        val urlData =
            when (repositoryInfo.type.name.lowercase()) {
                "github" -> {
                    val parts = repositoryInfo.id.split("/")
                    if (parts.size != 2) {
                        return "GitHubリポジトリIDの形式が不正です: ${repositoryInfo.id}".left()
                    }
                    UrlData.GithubUrlData(owner = parts[0], repository = parts[1])
                }

                "modrinth" -> UrlData.ModrinthUrlData(id = repositoryInfo.id)
                "spigotmc" -> UrlData.SpigotMcUrlData(resourceId = repositoryInfo.id)
                else -> return "未対応のリポジトリタイプです: ${repositoryInfo.type.name}".left()
            }

        // メタデータからバージョン情報を作成
        val versionData = VersionData(mpmInfo.download.downloadId, mpmInfo.version.current.raw)

        val downloadedFile =
            try {
                downloaderRepository.downloadByVersion(
                    urlData,
                    versionData,
                    mpmInfo.fileNamePattern
                )
            } catch (e: Exception) {
                return "プラグインのダウンロードに失敗しました (${repositoryInfo.type.name}: ${repositoryInfo.id}): ${e.message}".left()
            }

        if (downloadedFile == null) {
            return "プラグインファイルのダウンロードに失敗しました (${repositoryInfo.type.name}: ${repositoryInfo.id})。".left()
        }

        val template = mpmInfo.fileNameTemplate ?: "<pluginInfo.name>-<mpmInfo.version.current.normalized>.jar"
        val fileName = generateFileName(template, pluginInfo.name, mpmInfo.version.current.normalized)

        val pluginsDir = pluginDirectory.getPluginsDirectory()
        val targetFile = File(pluginsDir, fileName)
        try {
            downloadedFile.copyTo(targetFile, overwrite = true)
            downloadedFile.delete()
        } catch (e: Exception) {
            return "プラグインファイルの移動に失敗しました: ${e.message}".left()
        }

        return Unit.right()
    }

    private fun generateFileName(
        template: String,
        pluginName: String,
        versionString: String
    ): String {
        data class PluginInfo(
            val name: String
        )

        data class CurrentVersion(
            val normalized: String
        )

        data class MpmInfoVersion(
            val current: CurrentVersion
        )

        data class MpmInfo(
            val version: MpmInfoVersion
        )

        data class FileNameData(
            val pluginInfo: PluginInfo,
            val mpmInfo: MpmInfo
        )

        val data =
            FileNameData(
                pluginInfo = PluginInfo(name = pluginName),
                mpmInfo = MpmInfo(version = MpmInfoVersion(current = CurrentVersion(normalized = versionString)))
            )

        return template.replaceTemplate(data)
    }
}