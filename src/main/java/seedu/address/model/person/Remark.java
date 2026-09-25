package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

public class Remark {
    public String value;

    public Remark(String value){
        requireNonNull(value);
        this.value = value;
    }

    @Override
    public String toString(){
        return value;
    }

    @Override
    public boolean equals(Object other){
        return other == this || (other instanceof Remark) && value.equals(((Remark) other).value);

    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
