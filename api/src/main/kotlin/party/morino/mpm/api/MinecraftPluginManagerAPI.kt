/*
 * Written in 2023-2025 by Nikomaru <nikomaru@nikomaru.dev>
 *
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide.This software is distributed without any warranty.
 *
 * You should have received a copy of the CC0 Public Domain Dedication along with this software.
 * If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package party.morino.mpm.api

import party.morino.mpm.api.config.PluginDirectory
import party.morino.mpm.api.core.config.ConfigManager
import party.morino.mpm.api.core.plugin.PluginManager
import party.morino.mpm.api.core.plugin.ProjectManager
import party.morino.mpm.api.core.repository.RepositoryManager

/**
 * MinecraftPluginManagerのAPIインターフェース
 *
 * 外部プラグインやアドオンがMinecraftPluginManagerの機能にアクセスするための公開API
 * 各マネージャーへのアクセサを提供する
 */
interface MinecraftPluginManagerAPI {
    /**
     * 設定管理マネージャーを取得
     *
     * config.jsonの読み込み・再読み込みを担当するマネージャー
     *
     * @return ConfigManagerのインスタンス
     */
    fun getConfigManager(): ConfigManager

    /**
     * プラグインディレクトリマネージャーを取得
     *
     * プラグインディレクトリやデータディレクトリのパス管理を担当するマネージャー
     *
     * @return PluginDirectoryのインスタンス
     */
    fun getPluginDirectory(): PluginDirectory

    /**
     * プラグインマネージャーを取得
     *
     * プラグインの統合管理を担当するマネージャー
     * 以下の機能を提供する：
     * - メタデータ管理（metadata/xxx.yamlファイルの作成・更新・読み込み・保存）
     * - 情報管理（プラグイン情報の取得、リスト表示、バージョン確認）
     * - ライフサイクル管理（追加、削除、インストール、アンインストール）
     * - 更新管理（更新、ロック、一括インストール）
     *
     * @return PluginManagerのインスタンス
     */
    fun getPluginManager(): PluginManager

    /**
     * プロジェクトマネージャーを取得
     *
     * プロジェクトの検索とメタデータ取得を担当するマネージャー
     *
     * @return ProjectManagerのインスタンス
     */
    fun getProjectManager(): ProjectManager

    /**
     * リポジトリマネージャーを取得
     *
     * リポジトリソースの管理を担当するマネージャー
     *
     * @return RepositoryManagerのインスタンス
     */
    fun getRepositoryManager(): RepositoryManager
}