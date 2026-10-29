package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.EditProjectCommand.EditProjectDescriptor;
import seedu.address.model.project.Deadline;
import seedu.address.model.project.Description;
import seedu.address.model.project.ProjectName;

public class EditProjectDescriptorTest {

    private static final LocalDate NEW_DEADLINE = LocalDate.of(2027, 1, 1);

    @Test
    public void descriptorEquals() {
        EditProjectDescriptor descriptor = new EditProjectDescriptor();
        descriptor.setName(new ProjectName("Updated Project"));
        descriptor.setDeadline(new Deadline(NEW_DEADLINE));
        descriptor.setDescription(new Description("Updated description"));

        EditProjectDescriptor copy = new EditProjectDescriptor(descriptor);

        assertTrue(descriptor.equals(descriptor));
        assertEquals(descriptor, copy);
        assertEquals(copy, descriptor);
        assertFalse(descriptor.equals(null));
        assertFalse(descriptor.equals(1));

        EditProjectDescriptor differentName = new EditProjectDescriptor(descriptor);
        differentName.setName(new ProjectName("Another Name"));
        assertNotEquals(descriptor, differentName);

        EditProjectDescriptor differentDeadline = new EditProjectDescriptor(descriptor);
        differentDeadline.setDeadline(new Deadline(NEW_DEADLINE.plusDays(1)));
        assertNotEquals(descriptor, differentDeadline);

        EditProjectDescriptor differentDescription = new EditProjectDescriptor(descriptor);
        differentDescription.setDescription(new Description("Another description"));
        assertNotEquals(descriptor, differentDescription);

        assertEquals(new EditProjectDescriptor(), new EditProjectDescriptor());
        assertNotEquals(descriptor, new EditProjectDescriptor());
        assertNotEquals(new EditProjectDescriptor(), descriptor);
    }

    @Test
    public void descriptorCopy_originalModified_copyUnchanged() {
        EditProjectDescriptor original = new EditProjectDescriptor();
        original.setName(new ProjectName("Original Name"));
        original.setDeadline(new Deadline(NEW_DEADLINE));
        original.setDescription(new Description("Original description"));

        EditProjectDescriptor copy = new EditProjectDescriptor(original);

        original.setName(new ProjectName("Changed Name"));
        original.setDeadline(new Deadline(NEW_DEADLINE.plusDays(1)));
        original.setDescription(Description.EMPTY);

        assertEquals(new ProjectName("Original Name"), copy
            .getName()
            .orElseThrow());
        assertEquals(new Deadline(NEW_DEADLINE), copy
            .getDeadline()
            .orElseThrow());
        assertEquals(new Description("Original description"),
            copy
                .getDescription()
                .orElseThrow());
    }

    @Test
    public void descriptor_descriptionOmittedVersusCleared_distinctStates() {
        EditProjectDescriptor omitted = new EditProjectDescriptor();
        EditProjectDescriptor cleared = new EditProjectDescriptor();
        cleared.setDescription(Description.EMPTY);

        assertFalse(omitted.isAnyFieldEdited());
        assertTrue(omitted
            .getName()
            .isEmpty());
        assertTrue(omitted
            .getDeadline()
            .isEmpty());
        assertTrue(omitted
            .getDescription()
            .isEmpty());

        assertTrue(cleared.isAnyFieldEdited());
        assertEquals(Description.EMPTY, cleared
            .getDescription()
            .orElseThrow());
        assertNotEquals(omitted, cleared);

        EditProjectDescriptor copiedClear = new EditProjectDescriptor(cleared);
        assertTrue(copiedClear.isAnyFieldEdited());
        assertEquals(Description.EMPTY, copiedClear
            .getDescription()
            .orElseThrow());
    }
}
