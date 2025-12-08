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

import arrow.core.Either

/**
 * プラグインの一括インストールを行うユースケース
 * mpm.jsonを読み込み、metadataファイルとの差分を検出してインストールする
 */
interface BulkInstallUseCase {
    /**
     * mpm.jsonに定義されたすべてのプラグインをインストールする
     * metadataファイルが存在しないか、バージョンが異なるプラグインのみインストールする
     *
     * @return 成功時はインストール結果、失敗時はエラーメッセージ
     */
    suspend fun installAll(): Either<String, BulkInstallResult>
}