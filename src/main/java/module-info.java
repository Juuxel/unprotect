module io.github.juuxel.unprotect {
	requires org.objectweb.asm.tree;
	requires cpw.mods.modlauncher;
	requires static org.jetbrains.annotations;

	provides cpw.mods.modlauncher.serviceapi.ILaunchPluginService with juuxel.unprotect.UnprotectLaunchPlugin;
}
