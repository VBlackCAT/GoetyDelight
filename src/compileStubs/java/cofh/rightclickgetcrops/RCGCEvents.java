package cofh.rightclickgetcrops;

import net.minecraftforge.event.entity.player.PlayerInteractEvent;

/**
 * 编译期占位类，仅用于 Mixin 注解处理器解析可选目标。
 * 该 source set 不会进入运行时 classpath / 最终 jar；实际运行时使用 RCGC 模组自己的类。
 */
public final class RCGCEvents {

    private RCGCEvents() {
    }

    public static void handleRightClickBlockEvent(PlayerInteractEvent.RightClickBlock event) {
    }
}
