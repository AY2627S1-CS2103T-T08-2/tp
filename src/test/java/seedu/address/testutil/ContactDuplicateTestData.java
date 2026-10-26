package seedu.address.testutil;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonId;
import seedu.address.model.person.Phone;
import seedu.address.model.tag.Tag;

/** Provides independently controlled differences in the five duplicate-contact fields. */
public class ContactDuplicateTestData {
    public static final int NAME = 1;
    public static final int PHONE = 2;
    public static final int EMAIL = 4;
    public static final int ADDRESS = 8;
    public static final int TAGS = 16;
    public static final int ALL_FIELDS = 31;
    public static final PersonId FIRST_ID = PersonId.fromString("00000000-0000-0000-0000-000000000001");
    public static final PersonId SECOND_ID = PersonId.fromString("00000000-0000-0000-0000-000000000002");
    public static final PersonId THIRD_ID = PersonId.fromString("00000000-0000-0000-0000-000000000003");
    public static final Person BASE = person(0, FIRST_ID);
    public static final Person UNRELATED = new PersonBuilder().withName("Unrelated Contact")
            .withPersonId(THIRD_ID).build();

    /**
     * Creates a contact whose indicated fields differ from the base contact.
     * Each bit corresponds to one of NAME, PHONE, EMAIL, ADDRESS, and TAGS.
     */
    public static Person person(int differences, PersonId id) {
        Set<Tag> tags = (differences & TAGS) == 0 ? Set.of(new Tag("friend"), new Tag("classmate"))
                : Set.of(new Tag("friend"), new Tag("colleague"));
        return new Person(id,
                new Name((differences & NAME) == 0 ? "Alex Tan" : "Blair Tan"),
                new Phone((differences & PHONE) == 0 ? "123" : "124"),
                new Email((differences & EMAIL) == 0 ? "alex@example.com" : "blair@example.com"),
                new Address((differences & ADDRESS) == 0 ? "Block 1" : "Block 2"), tags);
    }

    /** Creates an edit descriptor that restores the indicated fields to the base values. */
    public static EditPersonDescriptor restoreBaseFields(int fields) {
        EditPersonDescriptor descriptor = new EditPersonDescriptor();
        if ((fields & NAME) != 0) {
            descriptor.setName(BASE.getName());
        }
        if ((fields & PHONE) != 0) {
            descriptor.setPhone(BASE.getPhone());
        }
        if ((fields & EMAIL) != 0) {
            descriptor.setEmail(BASE.getEmail());
        }
        if ((fields & ADDRESS) != 0) {
            descriptor.setAddress(BASE.getAddress());
        }
        if ((fields & TAGS) != 0) {
            descriptor.setTags(BASE.getTags());
        }
        return descriptor;
    }

    /** Returns an add command with the given contact's field values. */
    public static String addCommand(Person person) {
        String tags = person.getTags().stream().map(tag -> tag.tagName).sorted()
                .map(tag -> " t/" + tag).collect(Collectors.joining());
        return "add n/" + person.getName() + " p/" + person.getPhone() + " e/" + person.getEmail()
                + " a/" + person.getAddress() + tags;
    }

    /** Returns one of two list orders, putting the edited contact before or after its possible duplicate. */
    public static List<Person> contactOrder(Person target, int order) {
        return order == 0 ? List.of(target, UNRELATED, BASE) : List.of(BASE, UNRELATED, target);
    }
}
