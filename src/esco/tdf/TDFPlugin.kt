package esco.tdf

import arc.Events
import mindustry.mod.Plugin
import plugin.events.EscoPluginLoadEvent

class TDFPlugin : Plugin() {
    override fun init() {
        Events.on(EscoPluginLoadEvent::class.java) { _ ->
            TDGamemode.load()
        }
    }
}