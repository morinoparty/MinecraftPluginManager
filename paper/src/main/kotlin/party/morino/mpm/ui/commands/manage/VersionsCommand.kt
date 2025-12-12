/*
 * Written in 2023-2025 by Nikomaru <nikomaru@nikomaru.dev>
 *
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide.This software is distributed without any warranty.
 *
 * You should have received a copy of the CC0 Public Domain Dedication along with this software.
 * If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package party.morino.mpm.ui.commands.manage

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.CommandSender
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Flag
import org.incendo.cloud.annotations.Permission
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.mpm.api.core.plugin.PluginVersionsUseCase

/**
 * プラグインバージョン一覧表示コマンドのコントローラー
 * プレゼンテーション層とユースケース層の橋渡しを行う
 * mpm versions <plugin> [--limit <数>] - プラグインの利用可能なバージョン一覧を表示
 */
@Command("mpm")
@Permission("mpm.command")
class VersionsCommand : KoinComponent {
    // Koinによる依存性注入
    private val pluginVersionsUseCase: PluginVersionsUseCase by inject()

    /**
     * 指定されたプラグインの利用可能なバージョン一覧を表示するコマンド
     * @param sender コマンド送信者
     * @param plugin プラグイン名
     * @param limit 表示するバージョンの最大数（デフォルト: 20）
     */
    @Command("versions <plugin>")
    suspend fun versions(
        sender: CommandSender,
        @Argument("plugin") plugin: String,
        @Flag("limit") limit: Int = 20
    ) {
        sender.sendMessage(
            Component.text("プラグイン '$plugin' のバージョン一覧を取得しています...", NamedTextColor.GRAY)
        )

        // ユースケースを実行
        pluginVersionsUseCase.getVersions(plugin).fold(
            // 失敗時の処理
            { errorMessage ->
                sender.sendMessage(
                    Component.text(errorMessage, NamedTextColor.RED)
                )
            },
            // 成功時の処理
            { versions ->
                if (versions.isEmpty()) {
                    sender.sendMessage(
                        Component.text("プラグイン '$plugin' のバージョンが見つかりませんでした。", NamedTextColor.YELLOW)
                    )
                } else {
                    // 表示件数を制限
                    val displayVersions = versions.take(limit)
                    val totalCount = versions.size
                    val displayCount = displayVersions.size

                    // ヘッダー表示
                    val headerMessage =
                        if (totalCount > limit) {
                            "=== プラグイン '$plugin' のバージョン一覧 ($displayCount/${totalCount}件を表示) ==="
                        } else {
                            "=== プラグイン '$plugin' のバージョン一覧 (${totalCount}件) ==="
                        }
                    sender.sendMessage(
                        Component.text(headerMessage, NamedTextColor.GREEN)
                    )

                    // バージョン一覧を表示
                    displayVersions.forEachIndexed { index, version ->
                        // 最新版を強調表示
                        if (index == 0) {
                            sender.sendMessage(
                                Component.text("  - $version (最新)", NamedTextColor.AQUA)
                            )
                        } else {
                            sender.sendMessage(
                                Component.text("  - $version", NamedTextColor.WHITE)
                            )
                        }
                    }

                    // フッター表示
                    if (totalCount > limit) {
                        sender.sendMessage(
                            Component.text("残り${totalCount - displayCount}件のバージョンがあります。", NamedTextColor.GRAY)
                        )
                        sender.sendMessage(
                            Component.text("すべて表示するには --limit $totalCount を指定してください。", NamedTextColor.GRAY)
                        )
                    }
                    sender.sendMessage(
                        Component.text("=".repeat(50), NamedTextColor.GREEN)
                    )
                }
            }
        )
    }
}