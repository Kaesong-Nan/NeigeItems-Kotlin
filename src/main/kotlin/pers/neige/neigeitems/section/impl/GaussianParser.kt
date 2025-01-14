package pers.neige.neigeitems.section.impl

import org.bukkit.OfflinePlayer
import org.bukkit.configuration.ConfigurationSection
import pers.neige.neigeitems.section.SectionParser
import pers.neige.neigeitems.utils.ScriptUtils.toRoundingMode
import pers.neige.neigeitems.utils.SectionUtils.parseSection
import java.util.concurrent.ThreadLocalRandom

/**
 * gaussian节点解析器
 */
object GaussianParser : SectionParser() {
    override val id: String = "gaussian"

    override fun onRequest(
        data: ConfigurationSection,
        cache: MutableMap<String, String>?,
        player: OfflinePlayer?,
        sections: ConfigurationSection?
    ): String? {
        return handler(
            cache,
            player,
            sections,
            true,
            data.getString("base"),
            data.getString("spread"),
            data.getString("maxSpread"),
            data.getString("fixed"),
            data.getString("min"),
            data.getString("max"),
            data.getString("mode")
        )
    }

    override fun onRequest(
        args: List<String>,
        cache: MutableMap<String, String>?,
        player: OfflinePlayer?,
        sections: ConfigurationSection?
    ): String {
        return handler(
            cache,
            player,
            sections,
            false,
            args.getOrNull(0),
            args.getOrNull(1),
            args.getOrNull(2),
            args.getOrNull(3),
            args.getOrNull(4),
            args.getOrNull(5),
            args.getOrNull(6)
        ) ?: "<$id::${args.joinToString("_")}>"
    }

    /**
     * @param cache 解析值缓存
     * @param player 待解析玩家
     * @param sections 节点池
     * @param parse 是否对参数进行节点解析
     * @param baseString 基础数值文本
     * @param spreadString 浮动单位文本
     * @param maxSpreadString 浮动范围上限文本
     * @param fixedString 取整位数文本
     * @param minString 最小值文本
     * @param maxString 最大值文本
     * @param roundingMode 取整模式
     * @return 解析值
     */
    private fun handler(
        cache: MutableMap<String, String>?,
        player: OfflinePlayer?,
        sections: ConfigurationSection?,
        parse: Boolean,
        baseString: String?,
        spreadString: String?,
        maxSpreadString: String?,
        fixedString: String?,
        minString: String?,
        maxString: String?,
        roundingMode: String?
    ): String? {
        // 获取基础数值
        val base = baseString?.parseSection(parse, cache, player, sections)?.toDoubleOrNull()
        // 获取浮动单位
        val spread = spreadString?.parseSection(parse, cache, player, sections)?.toDoubleOrNull()
        // 获取浮动范围上限
        val maxSpread = maxSpreadString?.parseSection(parse, cache, player, sections)?.toDoubleOrNull()
        // 获取取整位数
        val fixed = fixedString?.parseSection(parse, cache, player, sections)?.toIntOrNull() ?: 1
        // 获取取整模式
        val mode = roundingMode.toRoundingMode()
        // 获取大小范围
        val min = minString?.parseSection(parse, cache, player, sections)?.toDoubleOrNull()
        val max = maxString?.parseSection(parse, cache, player, sections)?.toDoubleOrNull()
        // 获取随机数
        if (base != null && spread != null && maxSpread != null) {
            // 根据正态分布进行范围随机
            val random = (ThreadLocalRandom.current().nextGaussian()*spread)
                // 限制随机范围下限
                .coerceAtLeast(-maxSpread)
                // 限制随机范围上限
                .coerceAtMost(maxSpread)
            var result = base*(1 + random)
            // 限制结果下限
            min?.let { result = result.coerceAtLeast(it) }
            // 限制结果上限
            max?.let { result = result.coerceAtMost(it) }
            // 返回结果(基础数值+基础数值*浮动范围)
            return result.toBigDecimal().setScale(fixed, mode).toString()
        }
        return null
    }
}