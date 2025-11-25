/*
 * Written in 2023-2025 by Nikomaru <nikomaru@nikomaru.dev>
 *
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide.This software is distributed without any warranty.
 *
 * You should have received a copy of the CC0 Public Domain Dedication along with this software.
 * If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package party.morino.mpm.ui.commands

import be.seeseemelk.mockbukkit.ServerMock
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.mpm.MinecraftPluginManagerTest

@ExtendWith(MinecraftPluginManagerTest::class)
class InstallCommandTest : KoinComponent {
    val serverMock: ServerMock by inject()
    val urls =
        listOf(
            "https://modrinth.com/plugin/quickshop-hikari"
        )

    @Test
    fun installCommand() {
        val player = serverMock.addPlayer()
        player.isOp = true
        urls.forEach { url ->
            player.performCommand("mpm install $url")
            println(player.nextMessage())
        }
    }
}