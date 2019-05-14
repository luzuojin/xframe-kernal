package dev.xframe.modular.code;

public class ModularElement {
    public final Class<?> clazz;
    public final int index;
    public final boolean isAgent;
    public final Class<?> sharable;
    public Class<?> proxy;
    public ModularElement(Class<?> clazz, int index) {
        this.clazz = clazz;
        this.index = index;
        this.isAgent = ModularAnalyzer.isAgent(clazz);
        this.sharable = ModularAnalyzer.getSharableClass(clazz);
    }
    public boolean hasSharable() {
        return sharable != null;
    }
    public String getSharableName() {
        return sharable == null ? "" : sharable.getName();
    }
}