package ru.yandex.ydb.table.types;

/**
 * @author Sergey Polovko
 */
public final class VariantType implements Type {

    private final Type[] itemTypes;

    private VariantType(Type[] itemTypes) {
        this.itemTypes = itemTypes;
    }

    public static VariantType of(Type[] itemTypes) {
        return new VariantType(itemTypes.clone());
    }

    /**
     * will not clone given array
     */
    public static VariantType ofOwning(Type[] itemTypes) {
        return new VariantType(itemTypes);
    }

    public int getItemsCount() {
        return itemTypes.length;
    }

    public Type getItemType(int index) {
        return itemTypes[index];
    }

    @Override
    public Kind getKind() {
        return Kind.VARIANT;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || o.getClass() != VariantType.class) return false;

        VariantType variantType = (VariantType) o;
        int itemsCount = getItemsCount();
        if (itemsCount != variantType.getItemsCount()) {
            return false;
        }
        for (int i = 0; i < itemsCount; i++) {
            if (!getItemType(i).equals(variantType.getItemType(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int h = 31 * Kind.VARIANT.hashCode();
        for (int i = 0, count = getItemsCount(); i < count; i++) {
            h = 31 * h + getItemType(i).hashCode();
        }
        return h;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(128);
        sb.append("Variant<");
        int count = getItemsCount();
        for (int i = 0; i < count; i++) {
            sb.append(getItemType(i)).append(", ");
        }
        if (count != 0) {
            sb.setLength(sb.length() - 1); // cut last comma
        }
        sb.append('>');
        return sb.toString();
    }
}
