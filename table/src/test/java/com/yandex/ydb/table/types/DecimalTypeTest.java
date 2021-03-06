package com.yandex.ydb.table.types;

import com.google.common.truth.extensions.proto.ProtoTruth;
import com.yandex.ydb.ValueProtos;
import com.yandex.ydb.table.values.DecimalType;
import com.yandex.ydb.table.values.Type;
import com.yandex.ydb.table.values.proto.ProtoType;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;


/**
 * @author Sergey Polovko
 */
public class DecimalTypeTest {

    @Test
    public void contract() {
        DecimalType t = DecimalType.of(13, 2);

        assertThat(t.getKind()).isEqualTo(Type.Kind.DECIMAL);
        assertThat(t.getPrecision()).isEqualTo(13);
        assertThat(t.getScale()).isEqualTo(2);

        assertThat(t).isEqualTo(DecimalType.of(13, 2));
        assertThat(t).isNotEqualTo(DecimalType.of(11, 2));
        assertThat(t).isNotEqualTo(DecimalType.of(13, 1));

        assertThat(t.toString()).isEqualTo("Decimal(13, 2)");
    }

    @Test
    public void protobuf() {
        DecimalType type = DecimalType.of(10, 5);
        ValueProtos.Type typePb = type.toPb();

        ProtoTruth.assertThat(typePb).isEqualTo(ProtoType.decimal(10, 5));

        Type typeX = ProtoType.fromPb(typePb);
        assertThat(type).isEqualTo(typeX);
    }
}
