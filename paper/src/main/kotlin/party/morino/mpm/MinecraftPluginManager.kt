/*
 * Written in 2023-2025 by Nikomaru <nikomaru@nikomaru.dev>
 *
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide.This software is distributed without any warranty.
 *
 * You should have received a copy of the CC0 Public Domain Dedication along with this software.
 * If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package party.morino.mpm

import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.context.GlobalContext
import org.koin.dsl.module
import party.morino.mpm.api.MinecraftPluginManagerAPI
import party.morino.mpm.api.config.PluginDirectory
import party.morino.mpm.api.core.config.ConfigManager
import party.morino.mpm.api.core.plugin.DownloaderRepository
import party.morino.mpm.api.core.plugin.PluginManager
import party.morino.mpm.api.core.plugin.PluginRepository
import party.morino.mpm.api.core.plugin.ProjectManager
import party.morino.mpm.api.core.repository.RepositoryManager
import party.morino.mpm.config.PluginDirectoryImpl
import party.morino.mpm.core.config.ConfigManagerImpl
import party.morino.mpm.core.plugin.PluginManagerImpl
import party.morino.mpm.core.plugin.ProjectManagerImpl
import party.morino.mpm.core.plugin.infrastructure.DownloaderRepositoryImpl
import party.morino.mpm.core.plugin.infrastructure.PluginRepositoryImpl
import party.morino.mpm.core.repository.RepositorySourceManagerFactory

/**
 * MinecraftPluginManagerのメインクラス
 * プラグインの起動・終了処理やDIコンテナの設定を担当
 */
open class MinecraftPluginManager :
    JavaPlugin(),
    MinecraftPluginManagerAPI {
    // 各マネージャーのインスタンスをKoinから遅延初期化
    private val _configManager: ConfigManager by lazy { GlobalContext.get().get() }
    private val _pluginDirectory: PluginDirectory by lazy { GlobalContext.get().get() }
    private val _pluginManager: PluginManager by lazy { GlobalContext.get().get() }
    private val _projectManager: ProjectManager by lazy { GlobalContext.get().get() }
    private val _repositoryManager: RepositoryManager by lazy { GlobalContext.get().get() }

    /**
     * プラグイン有効化時の処理
     * DIコンテナの初期化を行う
     */
    override fun onEnable() {
        // DIコンテナの初期化
        setupKoin()
        logger.info("MinecraftPluginManager has been enabled!")
    }

    /**
     * プラグイン無効化時の処理
     */
    override fun onDisable() {
        logger.info("MinecraftPluginManager has been disabled!")
    }

    /**
     * Koinのセットアップ
     * 依存性注入の設定を行う
     */
    private fun setupKoin() {
        // モジュールの定義
        val appModule =
            module {
                // プラグインインスタンス
                single<MinecraftPluginManager> { this@MinecraftPluginManager }
                single<JavaPlugin> { this@MinecraftPluginManager }

                // 設定の登録（依存性はKoinのinjectによって自動注入される）
                single<PluginDirectory> { PluginDirectoryImpl() }
                single<ConfigManager> { ConfigManagerImpl() }

                // リポジトリマネージャーの登録（ファクトリーを使用）
                single<RepositoryManager> {
                    RepositorySourceManagerFactory.create(get(), get())
                }

                // リポジトリの登録（依存性はKoinのinjectによって自動注入される）
                single<DownloaderRepository> { DownloaderRepositoryImpl() }
                single<PluginRepository> { PluginRepositoryImpl() }

                // ドメイン単位のManagerの登録（Facadeパターンで関連UseCaseをまとめる）
                single<PluginManager> { PluginManagerImpl() }
                single<ProjectManager> { ProjectManagerImpl() }
            }

        // Koinの開始（すでに開始されている場合は何もしない）
        GlobalContext.getOrNull() ?: GlobalContext.startKoin {
            modules(appModule)
        }
    }

    // API getters - 式本体で簡潔に
    override fun getConfigManager(): ConfigManager = _configManager

    override fun getPluginDirectory(): PluginDirectory = _pluginDirectory

    override fun getPluginManager(): PluginManager = _pluginManager

    override fun getProjectManager(): ProjectManager = _projectManager

    override fun getRepositoryManager(): RepositoryManager = _repositoryManager
}