package seedu.address.model.project;

import java.util.List;
import java.util.function.Predicate;

import seedu.address.commons.util.StringUtil;
import seedu.address.commons.util.ToStringBuilder;

/**
 * Tests that a {@code Project}'s {@code ProjectName} matches any of the keywords given.
 */
public class ProjectNameContainsKeywordsPredicate implements Predicate<Project> {

    private final List<String> keywords;

    public ProjectNameContainsKeywordsPredicate(List<String> keywords) {
        this.keywords = keywords;
    }

    @Override
    public boolean test(Project project) {
        return keywords.stream()
            .anyMatch(keyword -> StringUtil.containsWordIgnoreCase(project.getName().projectName, keyword));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof ProjectNameContainsKeywordsPredicate otherProjectNameContainsKeywordsPredicate)) {
            return false;
        }

        return keywords.equals(otherProjectNameContainsKeywordsPredicate.keywords);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("keywords", keywords).toString();
    }
}
