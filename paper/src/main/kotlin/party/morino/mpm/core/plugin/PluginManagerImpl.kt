/*
 * Written in 2023-2025 by Nikomaru <nikomaru@nikomaru.dev>
 *
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * You should have received a copy of the CC0 Public Domain Dedication along with this software.
 * If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package party.morino.mpm.core.plugin

import arrow.core.Either
import party.morino.mpm.api.config.plugin.ManagedPlugin
import party.morino.mpm.api.core.plugin.BulkInstallResult
import party.morino.mpm.api.core.plugin.InstallResult
import party.morino.mpm.api.core.plugin.PluginManager
import party.morino.mpm.api.core.repository.RepositoryConfig
import party.morino.mpm.api.model.plugin.InstalledPlugin
import party.morino.mpm.api.model.plugin.OutdatedInfo
import party.morino.mpm.api.model.plugin.Plugin
import party.morino.mpm.api.model.plugin.PluginData
import party.morino.mpm.api.model.plugin.RepositoryPlugin
import party.morino.mpm.api.model.plugin.UpdateResult
import party.morino.mpm.api.model.repository.VersionData
import party.morino.mpm.core.plugin.infrastructure.PluginMetadataManagerImpl
import party.morino.mpm.core.plugin.usecase.AddPluginUseCaseImpl
import party.morino.mpm.core.plugin.usecase.BulkInstallUseCaseImpl
import party.morino.mpm.core.plugin.usecase.CheckOutdatedUseCaseImpl
import party.morino.mpm.core.plugin.usecase.LockPluginUseCaseImpl
import party.morino.mpm.core.plugin.usecase.PluginInstallUseCaseImpl
import party.morino.mpm.core.plugin.usecase.PluginListUseCaseImpl
import party.morino.mpm.core.plugin.usecase.PluginVersionsUseCaseImpl
import party.morino.mpm.core.plugin.usecase.RemovePluginUseCaseImpl
import party.morino.mpm.core.plugin.usecase.RemoveUnmanagedUseCaseImpl
import party.morino.mpm.core.plugin.usecase.UninstallPluginUseCaseImpl
import party.morino.mpm.core.plugin.usecase.UnlockPluginUseCaseImpl
import party.morino.mpm.core.plugin.usecase.UpdatePluginUseCaseImpl

/**
 * PluginManagerの実装クラス
 *
 * プラグインの統合管理を担当し、以下の機能を提供する：
 * - メタデータ管理（metadata/xxx.yamlファイルの作成・更新・読み込み・保存）
 * - 情報管理（プラグイン情報の取得、リスト表示、バージョン確認）
 * - ライフサイクル管理（追加、削除、インストール、アンインストール）
 * - 更新管理（更新、ロック、一括インストール）
 */
class PluginManagerImpl : PluginManager {
    // メタデータ管理
    private val metadataManager = PluginMetadataManagerImpl()

    // 情報管理用のUseCase
    private val pluginListUseCase = PluginListUseCaseImpl()
    private val pluginVersionsUseCase = PluginVersionsUseCaseImpl()
    private val checkOutdatedUseCase = CheckOutdatedUseCaseImpl()

    // ライフサイクル管理用のUseCase
    private val addPluginUseCase = AddPluginUseCaseImpl()
    private val removePluginUseCase = RemovePluginUseCaseImpl()
    private val pluginInstallUseCase = PluginInstallUseCaseImpl()
    private val uninstallPluginUseCase = UninstallPluginUseCaseImpl()
    private val removeUnmanagedUseCase = RemoveUnmanagedUseCaseImpl()

    // 更新管理用のUseCase
    private val updatePluginUseCase = UpdatePluginUseCaseImpl()
    private val lockPluginUseCase = LockPluginUseCaseImpl()
    private val unlockPluginUseCase = UnlockPluginUseCaseImpl()
    private val bulkInstallUseCase = BulkInstallUseCaseImpl()

    // キャッシュ用のプロパティ
    private var cachedManagedPlugins: List<PluginData>? = null
    private var cacheExpirationTime: Long = 0
    private val cacheTtlMillis = 180_000L // 3分 = 180秒 = 180,000ミリ秒

    // ========================================
    // メタデータ管理
    // ========================================

    override suspend fun createMetadata(
        pluginName: String,
        repository: RepositoryConfig,
        versionData: VersionData,
        action: String
    ): Either<String, ManagedPlugin> = metadataManager.createMetadata(pluginName, repository, versionData, action)

    override suspend fun updateMetadata(
        pluginName: String,
        versionData: VersionData,
        latestVersionData: VersionData,
        action: String
    ): Either<String, ManagedPlugin> =
        metadataManager.updateMetadata(pluginName, versionData, latestVersionData, action)

    override fun loadMetadata(pluginName: String): Either<String, ManagedPlugin> =
        metadataManager.loadMetadata(pluginName)

    override fun saveMetadata(
        pluginName: String,
        metadata: ManagedPlugin
    ): Either<String, Unit> = metadataManager.saveMetadata(pluginName, metadata)

    // ========================================
    // 情報管理
    // ========================================

    override suspend fun getAllManagedPlugins(): List<PluginData> {
        // 現在時刻を取得
        val currentTime = System.currentTimeMillis()

        // キャッシュが有効かチェック
        if (cachedManagedPlugins != null && currentTime < cacheExpirationTime) {
            return cachedManagedPlugins!!
        }

        // PluginListUseCaseに処理を委譲
        val result = pluginListUseCase.getAllManagedPlugins()

        // 結果をキャッシュに保存
        cachedManagedPlugins = result
        cacheExpirationTime = currentTime + cacheTtlMillis

        return result
    }

    override fun getAllServerPlugins(): Map<String, Boolean> = pluginListUseCase.getAllServerPlugins()

    override suspend fun getUnmanagedPlugins(): List<String> = pluginListUseCase.getUnmanagedPlugins()

    override suspend fun getEnabledPlugins(): List<PluginData> = pluginListUseCase.getEnabledPlugins()

    override suspend fun getDisabledPlugins(): List<PluginData> = pluginListUseCase.getDisabledPlugins()

    override suspend fun getVersions(plugin: RepositoryPlugin): Either<String, List<String>> =
        pluginVersionsUseCase.getVersions(plugin.pluginId)

    override suspend fun checkOutdated(plugin: InstalledPlugin): Either<String, OutdatedInfo> =
        checkOutdatedUseCase.checkOutdated(plugin.pluginId)

    override suspend fun checkAllOutdated(): Either<String, List<OutdatedInfo>> =
        checkOutdatedUseCase.checkAllOutdated()

    // ========================================
    // ライフサイクル管理
    // ========================================

    override suspend fun add(
        plugin: RepositoryPlugin,
        version: String
    ): Either<String, Unit> = addPluginUseCase.addPlugin(plugin.pluginId, version)

    override suspend fun remove(plugin: InstalledPlugin): Either<String, Unit> =
        removePluginUseCase.removePlugin(plugin.pluginId)

    override suspend fun install(plugin: Plugin): Either<String, InstallResult> =
        pluginInstallUseCase.installPlugin(plugin.pluginId)

    override suspend fun uninstall(plugin: InstalledPlugin): Either<String, Unit> =
        uninstallPluginUseCase.uninstallPlugin(plugin.pluginId)

    override suspend fun removeUnmanaged(): Either<String, List<String>> = removeUnmanagedUseCase.removeUnmanaged()

    // ========================================
    // 更新管理
    // ========================================

    override suspend fun update(): Either<String, List<UpdateResult>> = updatePluginUseCase.updatePlugins()

    override suspend fun lock(plugin: InstalledPlugin): Either<String, Unit> =
        lockPluginUseCase.lockPlugin(plugin.pluginId)

    override suspend fun unlock(plugin: InstalledPlugin): Either<String, Unit> =
        unlockPluginUseCase.unlockPlugin(plugin.pluginId)

    override suspend fun installAll(): Either<String, BulkInstallResult> = bulkInstallUseCase.installAll()
}