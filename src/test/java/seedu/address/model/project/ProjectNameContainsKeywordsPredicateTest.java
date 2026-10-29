package seedu.address.model.project;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.ProjectBuilder;

/**
 * Tests whole-word matching against project names.
 */
public class ProjectNameContainsKeywordsPredicateTest {

    private final Project project = new ProjectBuilder()
        .withName("CS2103T Team Project")
        .withDescription("Prepare presentation slides")
        .build();

    @Test
    public void test_emptyKeywords_returnsFalse() {
        assertFalse(new ProjectNameContainsKeywordsPredicate(List.of()).test(project));
    }

    @Test
    public void test_matchingWordsIgnoringCase_returnsTrue() {
        assertTrue(new ProjectNameContainsKeywordsPredicate(List.of("cs2103t")).test(project));
        assertTrue(new ProjectNameContainsKeywordsPredicate(List.of("TEAM")).test(project));
        assertTrue(new ProjectNameContainsKeywordsPredicate(List.of("pRoJeCt")).test(project));
    }

    @Test
    public void test_onlyOneKeywordMatches_returnsTrue() {
        assertTrue(new ProjectNameContainsKeywordsPredicate(List.of("Missing", "Team")).test(project));
    }

    @Test
    public void test_partialOrNonMatchingWords_returnsFalse() {
        assertFalse(new ProjectNameContainsKeywordsPredicate(List.of("CS2103")).test(project));
        assertFalse(new ProjectNameContainsKeywordsPredicate(List.of("Projec")).test(project));
        assertFalse(new ProjectNameContainsKeywordsPredicate(List.of("Orbital", "Design")).test(project));
    }

    @Test
    public void test_keywordOnlyInDescription_returnsFalse() {
        assertFalse(new ProjectNameContainsKeywordsPredicate(List.of("presentation", "slides")).test(project));
    }

    @Test
    public void equals_comparesKeywordLists() {
        ProjectNameContainsKeywordsPredicate predicate =
            new ProjectNameContainsKeywordsPredicate(List.of("Team"));

        assertTrue(predicate.equals(predicate));
        assertTrue(predicate.equals(new ProjectNameContainsKeywordsPredicate(List.of("Team"))));
        assertFalse(predicate.equals(new ProjectNameContainsKeywordsPredicate(List.of("Orbital"))));
        assertFalse(predicate.equals(null));
        assertFalse(predicate.equals("Team"));
    }
}
