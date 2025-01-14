package pers.neige.neigeitems.script.tool

import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import pers.neige.neigeitems.NeigeItems.bukkitScheduler
import pers.neige.neigeitems.NeigeItems.plugin
import pers.neige.neigeitems.hook.placeholderapi.PlaceholderExpansion
import pers.neige.neigeitems.manager.ConfigManager
import pers.neige.neigeitems.manager.ExpansionManager
import pers.neige.neigeitems.manager.HookerManager.papiHooker
import java.util.function.BiFunction

/**
 * papi扩展
 *
 * @property identifier 扩展名
 * @constructor papi扩展
 */
class ScriptPlaceholder(private val identifier: String) {
    private var author: String = "unknown"

    private var version: String = "1.0.0"

    private var executor: BiFunction<OfflinePlayer, String, String> =
        BiFunction<OfflinePlayer, String, String> { _, _ ->
            return@BiFunction ""
        }

    private var placeholderExpansion: PlaceholderExpansion? = null

    /**
     * 设置作者
     *
     * @param author 作者
     * @return ScriptPlaceholder本身
     */
    fun setAuthor(author: String): ScriptPlaceholder {
        this.author = author
        return this
    }

    /**
     * 设置版本
     *
     * @param version 版本
     * @return ScriptPlaceholder本身
     */
    fun setVersion(version: String): ScriptPlaceholder {
        this.version = version
        return this
    }

    /**
     * 设置papi变量处理器
     *
     * @param executor papi变量处理器
     * @return ScriptPlaceholder本身
     */
    fun setExecutor(executor: BiFunction<OfflinePlayer, String, String>): ScriptPlaceholder {
        this.executor = executor
        return this
    }

    /**
     * 注册papi扩展
     *
     * @return ScriptPlaceholder本身
     */
    fun register(): ScriptPlaceholder {
        // 存入ExpansionManager, 插件重载时自动取消注册
        ExpansionManager.placeholders[identifier]?.unRegister()
        ExpansionManager.placeholders[identifier] = this
        papiHooker?.newPlaceholderExpansion(identifier, author, version, executor)?.also {
            // papi是用HashMap存的扩展, 得主线程注册, 防止出现线程安全问题
            if (Bukkit.isPrimaryThread()) {
                placeholderExpansion = it
                it.register()
            } else {
                bukkitScheduler.callSyncMethod(plugin) {
                    placeholderExpansion = it
                    it.register()
                }
            }
            // papiHooker为null说明没安装PlaceholderAPI
        } ?: let {
            // 后台进行提示
            Bukkit.getLogger().info(ConfigManager.config.getString("Messages.invalidPlugin")?.replace("{plugin}", "PlaceholderAPI"))
        }
        return this
    }

    /**
     * 卸载监听器
     *
     * @return ScriptListener本身
     */
    fun unRegister(): ScriptPlaceholder {
        if (Bukkit.isPrimaryThread()) {
            placeholderExpansion?.also {
                it.unregister()
            }
        } else {
            bukkitScheduler.callSyncMethod(plugin) {
                placeholderExpansion?.also {
                    it.unregister()
                }
            }
        }
        return this
    }
}