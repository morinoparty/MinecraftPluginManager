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

package party.morino.mpm.api.core.plugin

/**
 * プラグインインストール情報
 */
data class PluginInstallInfo(
    // プラグイン名
    val name: String,
    // 現在のバージョン（raw形式）
    val currentVersion: String,
    // 最新バージョン（raw形式）
    val latestVersion: String
)

/**
 * プラグイン削除情報
 */
data class PluginRemovalInfo(
    // プラグイン名
    val name: String,
    // 削除されたバージョン
    val version: String
)

/**
 * 単一プラグインのインストール結果
 */
data class InstallResult(
    // インストールされたプラグイン情報
    val installed: PluginInstallInfo,
    // 削除されたプラグイン情報（削除されなかった場合はnull）
    val removed: PluginRemovalInfo?
)

/**
 * 一括インストール結果
 */
data class BulkInstallResult(
    // インストール成功したプラグインの詳細情報
    val installed: List<PluginInstallInfo>,
    // 削除されたファイルの詳細情報
    val removed: List<PluginRemovalInfo>,
    // インストール失敗したプラグイン一覧（プラグイン名 -> エラーメッセージ）
    val failed: Map<String, String>
)