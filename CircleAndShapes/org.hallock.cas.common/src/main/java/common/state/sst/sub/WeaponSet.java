package common.state.sst.sub;

import common.state.spec.GameSpec;
import common.state.spec.attack.Weapon;
import common.util.json.*;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;

/** We can remove this class again, I think **/
public class WeaponSet implements Jsonable {

    private final HashSet<Weapon> weapons = new HashSet<>();

    public static WeaponSet parseWeaponSet(GameSpec spec, JsonReaderWrapperSpec reader, String keyName) {
        return null;
    }

    public void add(Weapon weapon) {
        weapons.add(weapon);
    }

    public void remove(Weapon weapon) {
        weapons.remove(weapon);
    }

    public Collection<Weapon> ohMy() {
        return weapons;
    }

    public boolean isEmpty() {
        return weapons.isEmpty();
    }

    public String getDisplayString() {
        LinkedList<String> strings = new LinkedList<>();
        for (Weapon weapon : weapons)
            strings.add(weapon.toString());
        Collections.sort(strings);
        StringBuilder builder = new StringBuilder();
        for (String string : strings)
            builder.append(string).append(", ");
        return builder.toString();
    }

    @Override
    public void writeTo(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
        writer.writeBeginDocument();
        writer.write("weapons", weapons, Weapon.Serializer, options);
        writer.writeEndDocument();
    }

    public static final DataSerializer<WeaponSet> Serializer = new DataSerializer.JsonableSerializer<WeaponSet>() {
        @Override
        public WeaponSet parse(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
            WeaponSet set = new WeaponSet();
            reader.readBeginDocument();
            reader.read("weapons", spec, set.ohMy(), Weapon.Serializer);
            reader.readEndDocument();
            return set;
        }
    };
}
