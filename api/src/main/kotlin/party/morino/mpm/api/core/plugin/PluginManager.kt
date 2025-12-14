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

package party.morino.mpm.api.core.plugin

import arrow.core.Either
import party.morino.mpm.api.config.plugin.ManagedPlugin
import party.morino.mpm.api.core.repository.RepositoryConfig
import party.morino.mpm.api.model.plugin.InstalledPlugin
import party.morino.mpm.api.model.plugin.OutdatedInfo
import party.morino.mpm.api.model.plugin.Plugin
import party.morino.mpm.api.model.plugin.PluginData
import party.morino.mpm.api.model.plugin.RepositoryPlugin
import party.morino.mpm.api.model.plugin.UpdateResult
import party.morino.mpm.api.model.repository.VersionData

/**
 * プラグインの統合管理インターフェース
 *
 * プラグインのメタデータ管理、情報取得、ライフサイクル管理、更新管理を担当する
 * 以下の機能を統合している：
 * - メタデータ管理（metadata/xxx.yamlファイルの作成・更新・読み込み・保存）
 * - 情報管理（プラグイン情報の取得、リスト表示、バージョン確認）
 * - ライフサイクル管理（追加、削除、インストール、アンインストール）
 * - 更新管理（更新、ロック、一括インストール）
 */
interface PluginManager {
    // ========================================
    // メタデータ管理
    // ========================================

    /**
     * 新しいプラグインのメタデータを作成する
     *
     * @param pluginName プラグイン名
     * @param repository リポジトリ設定
     * @param versionData バージョン情報
     * @param action 実行したアクション（"add", "update" など）
     * @return 成功時は作成されたメタデータ、失敗時はエラーメッセージ
     */
    suspend fun createMetadata(
        pluginName: String,
        repository: RepositoryConfig,
        versionData: VersionData,
        action: String = "add"
    ): Either<String, ManagedPlugin>

    /**
     * 既存のメタデータを更新する
     *
     * @param pluginName プラグイン名
     * @param versionData 新しいバージョン情報（インストールするバージョン）
     * @param latestVersionData 最新バージョン情報（latestフィールドの更新用）
     * @param action 実行したアクション（"update", "install" など）
     * @return 成功時は更新されたメタデータ、失敗時はエラーメッセージ
     */
    suspend fun updateMetadata(
        pluginName: String,
        versionData: VersionData,
        latestVersionData: VersionData,
        action: String = "update"
    ): Either<String, ManagedPlugin>

    /**
     * メタデータファイルからプラグインメタデータを読み込む
     *
     * @param pluginName プラグイン名
     * @return 成功時は読み込まれたメタデータ、失敗時はエラーメッセージ
     */
    fun loadMetadata(pluginName: String): Either<String, ManagedPlugin>

    /**
     * プラグインメタデータをファイルに保存する
     *
     * @param pluginName プラグイン名
     * @param metadata 保存するメタデータ
     * @return 成功時はUnit、失敗時はエラーメッセージ
     */
    fun saveMetadata(
        pluginName: String,
        metadata: ManagedPlugin
    ): Either<String, Unit>

    // ========================================
    // 情報管理
    // ========================================

    /**
     * 管理下のすべてのプラグインを取得
     * @return 管理下のプラグインのリスト
     */
    suspend fun getAllManagedPlugins(): List<PluginData>

    /**
     * サーバー上の全プラグインの状態を取得
     * @return プラグイン名とその状態（有効/無効）のマップ
     */
    fun getAllServerPlugins(): Map<String, Boolean>

    /**
     * 管理下にないプラグインを取得
     * @return 管理下にないプラグイン名のリスト
     */
    suspend fun getUnmanagedPlugins(): List<String>

    /**
     * 有効なプラグインのみを取得
     * @return 有効なプラグインのリスト
     */
    suspend fun getEnabledPlugins(): List<PluginData>

    /**
     * 無効なプラグインのみを取得
     * @return 無効なプラグインのリスト
     */
    suspend fun getDisabledPlugins(): List<PluginData>

    /**
     * 指定されたプラグインの利用可能なバージョン一覧を取得する
     *
     * @param plugin リポジトリプラグイン
     * @return 成功時はバージョンのリスト、失敗時はエラーメッセージ
     */
    suspend fun getVersions(plugin: RepositoryPlugin): Either<String, List<String>>

    /**
     * 指定されたプラグインの更新を確認する
     *
     * @param plugin インストール済みプラグイン
     * @return 成功時は更新情報、失敗時はエラーメッセージ
     */
    suspend fun checkOutdated(plugin: InstalledPlugin): Either<String, OutdatedInfo>

    /**
     * すべての管理下プラグインの更新を確認する
     *
     * @return 成功時は更新情報のリスト、失敗時はエラーメッセージ
     */
    suspend fun checkAllOutdated(): Either<String, List<OutdatedInfo>>

    // ========================================
    // ライフサイクル管理
    // ========================================

    /**
     * プラグインを管理対象に追加する
     * mpm.jsonのpluginsマップにプラグインを追加する
     *
     * @param plugin リポジトリプラグイン
     * @param version バージョン文字列（デフォルトは"latest"）
     * @return 成功時はUnit、失敗時はエラーメッセージ
     */
    suspend fun add(
        plugin: RepositoryPlugin,
        version: String = "latest"
    ): Either<String, Unit>

    /**
     * プラグインを管理対象から除外する（ファイルは削除されない）
     * mpm.jsonから削除するが、pluginsディレクトリからJARファイルは削除しない
     *
     * @param plugin インストール済みプラグイン
     * @return 成功時はUnit、失敗時はエラーメッセージ
     */
    suspend fun remove(plugin: InstalledPlugin): Either<String, Unit>

    /**
     * 単一のプラグインをインストールする
     *
     * @param plugin リポジトリプラグインまたはインストール済みプラグイン
     * @return 成功時はインストール結果、失敗時はエラーメッセージ
     */
    suspend fun install(plugin: Plugin): Either<String, InstallResult>

    /**
     * プラグインをアンインストールする（設定から削除し、ファイルも削除）
     * mpm.jsonから削除し、pluginsディレクトリからJARファイルも削除する
     *
     * @param plugin インストール済みプラグイン
     * @return 成功時はUnit、失敗時はエラーメッセージ
     */
    suspend fun uninstall(plugin: InstalledPlugin): Either<String, Unit>

    /**
     * mpm管理下にないプラグインを削除する
     * mpm.jsonに含まれていないプラグインのJARファイルを削除する
     *
     * @return 成功時は削除されたプラグイン名のリスト、失敗時はエラーメッセージ
     */
    suspend fun removeUnmanaged(): Either<String, List<String>>

    // ========================================
    // 更新管理
    // ========================================

    /**
     * 新しいバージョンがあるすべてのプラグインを更新する
     *
     * @return 成功時は更新結果のリスト、失敗時はエラーメッセージ
     */
    suspend fun update(): Either<String, List<UpdateResult>>

    /**
     * プラグインをロックして自動更新を防ぐ
     * プラグインのメタデータにロックフラグを設定する
     *
     * @param plugin インストール済みプラグイン
     * @return 成功時はUnit、失敗時はエラーメッセージ
     */
    suspend fun lock(plugin: InstalledPlugin): Either<String, Unit>

    /**
     * プラグインのロックを解除する
     * プラグインのメタデータからロックフラグを削除する
     *
     * @param plugin インストール済みプラグイン
     * @return 成功時はUnit、失敗時はエラーメッセージ
     */
    suspend fun unlock(plugin: InstalledPlugin): Either<String, Unit>

    /**
     * mpm.jsonに定義されたすべてのプラグインを一括インストールする
     * metadataファイルが存在しないか、バージョンが異なるプラグインのみインストールする
     *
     * @return 成功時はインストール結果、失敗時はエラーメッセージ
     */
    suspend fun installAll(): Either<String, BulkInstallResult>
}