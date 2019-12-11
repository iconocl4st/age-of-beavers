package common.state.spec.attack;

import common.state.sst.sub.Load;
import common.util.json.*;

import java.io.IOException;

public class Weapon implements Jsonable {
    public final WeaponSpec weaponType;
    public double condition;

    public Weapon(WeaponSpec spec) {
        this.weaponType = spec;
        this.condition = 1.0;
    }

    public String toString() {
        return weaponType.name;
    }

    public boolean hasAmmunition(Load carrying) {
        return carrying.canAfford(weaponType.fireResources);
    }

    public boolean equals(Object other) {
        return other instanceof Weapon && ((Weapon) other).weaponType.equals(weaponType) && ((Weapon) other).condition == condition;
    }

    public int hashCode() {
        return (weaponType.name + ":" + condition).hashCode();
    }

    @Override
    public void writeTo(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
        writer.writeBeginDocument();
        writer.write("weapon-type", weaponType.name);
        writer.write("condition",  condition);
        writer.writeEndDocument();
    }

    public static final DataSerializer<Weapon> Serializer = new DataSerializer.JsonableSerializer<Weapon>() {
        @Override
        public Weapon parse(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
            reader.readBeginDocument();
            Weapon weapon = new Weapon(spec.spec().getWeaponSpec(reader.readString("weapon-type")));
            weapon.condition = reader.readDouble("condition");
            reader.readEndDocument();
            return weapon;
        }
    };
}
