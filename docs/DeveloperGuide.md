---
  layout: default.md
  title: "Developer Guide"
  pageNav: 3
---

# UniTeam Developer Guide

--------------------------------------------------------------------------------------------------------------------

## **Acknowledgements**

This project is based on [AddressBook-Level3](https://github.com/se-edu/addressbook-level3), created by the [SE-EDU initiative](https://se-education.org).

--------------------------------------------------------------------------------------------------------------------

## **Setting up, getting started**

Refer to the guide [_Setting up and getting started_](SettingUp.md).

--------------------------------------------------------------------------------------------------------------------

## **Design**

### Architecture

<puml src="diagrams/ArchitectureDiagram.puml" width="280" />

The ***Architecture Diagram*** given above explains the high-level design of the App.

The following provides a quick overview of the main components and their interactions.

**Main components of the architecture**

**`Main`** (consisting of 
classes [`Main`](https://github.com/AY2627S1-CS2103T-T08-2/tp/tree/master/src/main/java/seedu/address/Main.java) and 
[`MainApp`](https://github.com/AY2627S1-CS2103T-T08-2/tp/tree/master/src/main/java/seedu/address/MainApp.java)) is 
in charge of the app launch and shut down.
* At app launch, it initialises the other components in the correct sequence, and connects them up with each other.
* At shut down, it shuts down the other components and invokes cleanup methods where necessary.

`Main#launchWithLock` acquires an exclusive file lock on `data/uniteam.lock`
before calling `Application.launch`. The lock remains held until the
application exits. A second instance using the same data folder logs a warning
and exits before application initialisation.

The lock is released when its resources close, and the operating system also
releases it when the process terminates. The lock file remains on disk;
its existence does not indicate that an instance is still running.

The bulk of the app's work is done by the following four components:

* [**`UI`**](#ui-component): The UI of the App.
* [**`Logic`**](#logic-component): The command executor.
* [**`Model`**](#model-component): Holds the data of the App in memory.
* [**`Storage`**](#storage-component): Reads data from, and writes data to, the hard disk.

[**`Commons`**](#common-classes) represents a collection of classes used by multiple other components.

**How the architecture components interact with each other**

The *Sequence Diagram* below shows how the components interact with each other for the scenario where the user issues the command `delete 1`.

<puml src="diagrams/ArchitectureSequenceDiagram.puml" width="574" />

Each of the four main components (also shown in the diagram above),

* defines its *API* in an `interface` with the same name as the Component.
* provides its functionality through a concrete `{Component Name}Manager` class that implements the corresponding API interface.

For example, the `Logic` component defines its API in `Logic.java` and implements it in `LogicManager.java`. Other components interact with a component through its interface rather than its concrete class, preventing them from coupling to that component's implementation, as illustrated in the following partial class diagram.

<puml src="diagrams/ComponentManagers.puml" width="300" />

The sections below give more details of each component.

### UI component

The **API** of this component is specified 
in [`Ui.java`](https://github.com/AY2627S1-CS2103T-T08-2/tp/tree/master/src/main/java/seedu/address/ui/Ui.java)

<puml src="diagrams/UiClassDiagram.puml" alt="Structure of the UI Component"/>

The UI consists of `MainWindow` and its parts, including `CommandBox`,
`ResultDisplay`, `PersonListPanel`, `ProjectListPanel`, `HelpWindow` and
`StatusBarFooter`. These components inherit from `UiPart`, which loads their
corresponding FXML layouts.

The Contacts tab displays `PersonCard` objects, while the Projects tab displays
`ProjectCard` objects. Both panels observe filtered lists supplied through
`Logic`, so changes to their contents are reflected in the UI.

`CommandResult` carries command feedback, help and exit flags, and a
`ListViewTarget`. `MainWindow` uses that target to select the Contacts or
Projects tab. A target of `NONE` preserves the currently selected tab.
Member roles appear in command feedback rather than on contact cards.

The `UI` component uses the JavaFX UI framework. The layouts of these UI parts are defined in matching `.fxml` files 
in `src/main/resources/view`. For example,
[`MainWindow.fxml`](https://github.com/AY2627S1-CS2103T-T08-2/tp/tree/master/src/main/resources/view/MainWindow.fxml)
specifies the layout of
[`MainWindow`](https://github.com/AY2627S1-CS2103T-T08-2/tp/tree/master/src/main/java/seedu/address/ui/MainWindow.java).

The `UI` component,

* executes user commands using the `Logic` component.
* listens for changes to `Model` data so that the UI can be updated with the modified data.
* keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
* depends on model classes such as `Person` and `Project` because their data
  is displayed in contact and project cards.

### Logic component

**API** : [`Logic.java`](https://github.com/AY2627S1-CS2103T-T08-2/tp/tree/master/src/main/java/seedu/address/logic/Logic.java)

Here's a (partial) class diagram of the `Logic` component:

<puml src="diagrams/LogicClassDiagram.puml" width="550"/>

The sequence diagram below illustrates the interactions within the `Logic` component, taking `execute("delete 1")` API call as an example.

<puml src="diagrams/DeleteSequenceDiagram.puml" alt="Interactions Inside the Logic Component for the `delete 1` Command" />

<box type="info" seamless>

**Note:** The lifeline for `DeleteCommandParser` should end at the destroy marker (X), but due to a limitation of PlantUML, the lifeline continues till the end of diagram.
</box>


How the `Logic` component works:

1. When `Logic` is called upon to execute a command, the command is passed to an `AddressBookParser` object, which in turn creates a parser that matches the command (e.g., `DeleteCommandParser`) and uses it to parse the command.
1. This results in a `Command` object (more precisely, an object of one of its subclasses e.g., `DeleteCommand`) which is executed by the `LogicManager`.
1. The command can communicate with the `Model` when it is executed (e.g. to delete a person).<br>
   Note that although this is shown as a single step in the diagram above for simplicity, the code can require several interactions between the command object and the `Model` to complete the operation.
1. The result of the command execution is encapsulated as a `CommandResult` object which is returned from `Logic`.

Here are the other classes in `Logic` (omitted from the class diagram above) that are used for parsing a user command:

<puml src="diagrams/ParserClasses.puml" width="600"/>

How the parsing works:
* When called upon to parse a user command, the `AddressBookParser` class creates an `XYZCommandParser` (`XYZ` is a placeholder for the specific command name, e.g., `AddCommandParser`). The parser uses the other classes shown above to parse the user command and create an `XYZCommand` object (e.g., `AddCommand`). The `AddressBookParser` returns that object as a `Command` object.
* All `XYZCommandParser` classes, such as `AddCommandParser` and `DeleteCommandParser`, implement the `Parser` interface so they can be treated similarly where appropriate, for example during testing.

### Model component
**API** : [`Model.java`](https://github.com/AY2627S1-CS2103T-T08-2/tp/tree/master/src/main/java/seedu/address/model/Model.java)

<puml src="diagrams/ModelClassDiagram.puml" width="650" />


The `Model` component,

* maintains independent `FilteredList<Person>` and `FilteredList<Project>`
  views over the address book's observable collections. These views are
  exposed through `getFilteredPersonList()` and `getFilteredProjectList()`
  for the UI to observe.
* gives each `Person` a non-null, immutable internal `PersonId`, represented by a UUID, that is not displayed to users.
* stores the `Person` objects selected by the current filter, such as search results, in a separate _filtered_ list. It exposes this list as an unmodifiable `ObservableList<Person>` that the UI can observe and bind to, so the UI updates when the list changes.
* stores a `UserPrefs` object that represents the user’s preferences (currently, just the GUI settings). This is exposed to the outside as a `ReadOnlyUserPrefs` object.
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)

Each `Person` owns exactly one `PersonId`, shown by the composition in the Model diagrams. The normal `Person` constructor generates a new ID, while the reconstruction constructor accepts an existing ID. Editing replaces the person's details but preserves its ID.

IDs provide stable internal references and are separate from contact duplicate detection. `Person#isSamePerson`
delegates to `Person#equals`, comparing name, phone, email, address, and tags without including `PersonId`.
Tags are compared as a set, so their order does not matter. `Person#hashCode` uses the same five fields.
Contacts with matching details remain duplicates even when their IDs differ; contacts sharing a name are allowed
when any other field differs. `UniquePersonList` separately enforces unique IDs.

`AddCommand` and `EditCommand` use `Model#hasPerson`, which checks the complete address book rather than the filtered
contact list. `UniquePersonList` enforces the same duplicate rule for additions, replacements, and bulk updates.
An unchanged edit is allowed; a changed contact must not match another contact's complete details.

A `ProjectMembership` represents the association between one contact and one project. It stores the contact's
`PersonId`, the project's `ProjectId`, and an immutable `Role` describing that contact's task allotment for the project;
it does not store copies of the corresponding `Person` or `Project`.
`AddressBook` maintains these associations in a `UniqueProjectMembershipList`, where each person-project ID pair may
occur at most once. A membership can be added only when both referenced entities exist in the same address book.
Deleting a person or project removes its related memberships, while editing either entity preserves its memberships
because its ID remains unchanged. Address book copies and resets include the membership collection, and callers can
inspect it only through an unmodifiable list.

`Model` exposes membership lookup, addition, removal, and role replacement through `hasProjectMembership`,
`addProjectMembership`, `removeProjectMembership`, and `setProjectMembership`. `ModelManager` delegates these operations
to `AddressBook`, which enforces membership uniqueness and valid entity references. Role replacement locates the
existing person-project pair and changes its immutable membership value without changing its identity. `AssociateCommand`
resolves the displayed contact index and project list index, validates both, and
creates a membership using the selected entities' IDs. It rejects an existing pair before modifying the model and
confirms success with both names. `AddressBookParser` routes `associate` to `AssociateCommandParser`, which requires
one `c/` prefix and one `pr/` prefix, rejects a non-empty preamble, and parses both values with `ParserUtil#parseIndex`.
Syntax errors include the command usage message, while repeated prefixes retain the standard duplicate-field
diagnostic. The parser checks positive-integer syntax; the command checks the indices against the model lists.

The Contacts tab displays `Model#getFilteredPersonList()` through
`Logic#getFilteredPersonList()` and `PersonListPanel`. The Projects tab displays
`Model#getFilteredProjectList()` through `Logic#getFilteredProjectList()` and
`ProjectListPanel`.

Contact and project indices refer to positions in their respective filtered
lists. Each displayed list is numbered starting from 1. Commands resolve an
index against the displayed list before modifying the selected entity.
The UUID shown on a project card is not a command index.

The complete collections remain available through
`Model#getAddressBook().getPersonList()` and
`Model#getAddressBook().getProjectList()`. Duplicate detection and exact project
lookup consider the complete data, including entities hidden by filters.

Switching tabs changes neither filter.

<box type="info" seamless>

**Note:** The alternative, arguably more object-oriented, design below keeps a unique list of tags in `AddressBook`, and each `Person` references tags from that list. This lets `AddressBook` maintain one `Tag` object per unique tag instead of each `Person` holding its own `Tag` objects.<br>

<puml src="diagrams/BetterModelClassDiagram.puml" width="450" />
</box>


### Storage component

**API** : [`Storage.java`](https://github.com/AY2627S1-CS2103T-T08-2/tp/tree/master/src/main/java/seedu/address/storage/Storage.java)

<puml src="diagrams/StorageClassDiagram.puml" width="550" />

The `Storage` component,
* can save both address book data and user preference data in JSON format, and read them back into corresponding objects.
* is implemented by `StorageManager`, which delegates the actual JSON file access to `JsonAddressBookStorage` and `JsonUserPrefsStorage` (one class per data file).
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)

`JsonAdaptedPerson` saves each contact's `PersonId` as an `id` string in the address book JSON file and restores it when loading. A missing or null `id` is supported for legacy data and generates a new ID, which is persisted on the next save. A present ID must use the standard UUID format, with either uppercase or lowercase hexadecimal digits. Malformed IDs cause an `IllegalValueException`, which `JsonAddressBookStorage` reports as a `DataLoadingException`.

`JsonSerializableAddressBook#toModelType()` also tracks restored IDs in a `Set<PersonId>` and rejects duplicate IDs
with a dedicated error message. UUIDs that differ only in capitalization represent the same ID. The duplicate-person
check runs first and compares all five contact fields through `AddressBook#hasPerson`; a contact is added only after
both checks pass. Same-name contacts with different details and distinct IDs can therefore be saved and loaded
together. Duplicate IDs are reported as invalid data through the same storage-loading exception path as malformed
IDs. The JSON format is unchanged, and memberships continue to reference contacts by their IDs.

#### Validation and loading recovery

`FileUtil#readFromFile` uses `Files.readString` to decode UTF-8 strictly.
Malformed byte sequences cause an `IOException` rather than being replaced
silently with replacement characters.

`JsonUtil` rejects null JSON roots, duplicate JSON property names and content
following the first JSON root value. Trailing whitespace is allowed.

`JsonSerializableAddressBook#toModelType` validates contacts, projects and
memberships before returning an address book. Validation includes duplicate
entities and IDs, invalid field values, null collection entries, null tags
and membership references to missing entities.

Missing or null top-level collections are treated as empty for compatibility
with older data. A missing or null stored membership role becomes `Role.EMPTY`.

Loading errors are reported through `DataLoadingException`.
`StorageManager` records an address-book loading failure and blocks address-book
saving for the remainder of that session.

`MainApp` starts with an empty address book after such a failure.
`UiManager` displays recovery instructions, and the status bar retains a
saving-disabled warning. A file confirmed to be missing instead follows
first-launch behaviour and loads sample data.

Preference-loading failures use default preferences. A null `guiSettings`
value is rejected by `JsonUserPrefsStorage` before model initialisation.

#### Atomic address-book saving

`JsonAddressBookStorage#saveAddressBook` writes the serialised data to a
temporary file in the destination directory. Once writing succeeds, it
replaces the target using `ATOMIC_MOVE` and `REPLACE_EXISTING`.

Writing to a temporary file avoids truncating the existing data file before
serialisation finishes. Temporary-file cleanup is attempted whether saving
succeeds or fails. Save failures propagate to `LogicManager`, which handles
model rollback or warning-only feedback.

#### Legacy migration

Loading legacy data generates IDs in memory but does not rewrite the JSON file. `LogicManager#execute()` saves the address book after every successful command, including commands such as `list` that do not change contact details. That save writes the generated IDs, and subsequent loads restore them.

`MainApp#stop()` saves user preferences only. Closing the window without executing a successful command leaves an ID-less legacy file unchanged, so its missing or null IDs are generated again on the next startup. Entering `exit` is different from closing the window: it is a successful command and follows the normal address-book save path.

Immediate migration write-back is not implemented. If IDs must remain stable across startups even when no address-book save occurs, the team must explicitly choose to add a write-back after successful legacy loading, including handling possible write failures.

### Common classes

Classes used by multiple components are in the `seedu.address.commons` package.

`LogsCenter` configures the parent `baseLogger` with console and file handlers.
Class-specific loggers inherit these handlers.

The console handler is installed before the file handler. If opening the log
file fails, `LogsCenter` reports the failure through the already-initialised
`baseLogger` and continues with console logging.

--------------------------------------------------------------------------------------------------------------------

## **Implementation**

This section describes some noteworthy details on how certain features are implemented.

### Project membership model

The project membership model represents the many-to-many relationship between contacts and projects: one contact can
belong to multiple projects, and one project can contain multiple contacts. Each relationship is represented by a
separate `ProjectMembership` value object containing a `PersonId`, a `ProjectId`, and that member's project-specific
`Role`.

Using stable IDs keeps memberships valid when descriptive fields such as a contact's phone number or a project's name
change. It also avoids the stale or inconsistent data that could result from storing copies of `Person` or `Project` in
each membership. Names and list indexes are unsuitable references because names can be edited and indexes change when
the displayed list is filtered or reordered.

`AddressBook` owns the person, project and membership collections and enforces
their relationship invariants. It rejects duplicate membership ID pairs and
memberships referencing unknown entities. Deleting a person or project removes
its related memberships.

Bulk replacement first copies the incoming collections into temporary unique
lists and validates membership references against the replacement data.
Only after validation succeeds are the live collections replaced. This prevents
invalid replacement data from partially changing the address book and preserves
data when a collection is replaced using itself or one of its live views.

`ListMembersCommand` resolves the supplied project name using
`Model#findProject`, which searches the complete project collection.
`AddressBook#getProjectMembers` returns the project's contacts in address-book
order. The command then installs a contact predicate that checks membership
using the selected project's stable ID. Member names and roles are formatted
in the command feedback, while the Contacts tab displays contact cards.

`SetRoleCommand` resolves both indices against the currently filtered lists,
checks that the membership exists, and replaces it with an immutable
`ProjectMembership` carrying the new role. `ProjectMembership#equals` and
`hashCode` use the person-project ID pair; the role is not part of membership
identity.

`SetRoleCommandParser` requires the `r/` prefix but allows its value to be empty.
`ParserUtil#parseRole` returns `Role.EMPTY` for an empty trimmed value. Clearing
a role therefore preserves the membership instead of dissociating the contact.

`ListProjectsCommand` optionally accepts `c/CONTACT_INDEX`. With no contact
index, it resets the project predicate to `PREDICATE_SHOW_ALL_PROJECTS`,
lists all projects and requests the Projects tab.

With a contact index, the command resolves the contact against
`Model#getFilteredPersonList()` and calls `Model#getPersonProjects()`.
`AddressBook#getPersonProjects()` matches membership IDs and returns associated
projects in address-book order.

Each associated project is numbered using its position in the current filtered
Projects list. A project hidden by the project filter is labelled
`[not displayed]` without an index. This avoids presenting a number that would
select a different displayed project.

The contact-specific form preserves both predicates and returns a
`ListViewTarget` of `NONE`, leaving the selected tab unchanged.

### Project management

`Project` is an immutable entity containing a stable `ProjectId`, a
`ProjectName`, a `Deadline` and a `Description`.

`UniqueProjectList` enforces unique project IDs and normalised names.
Name comparison currently uses a lowercase `Locale.ROOT` comparison key after
whitespace normalisation. Project replacement must preserve the stored ID.

`AddProjectCommandParser` requires a name and deadline and accepts an optional
description. `AddProjectCommand` checks for duplicates against the complete
project collection before adding the project. A successful addition clears
the project filter.

`EditProjectCommandParser` creates an `EditProjectDescriptor` containing only
the supplied fields. At least one field is required. An empty `desc/` supplies
`Description.EMPTY`, while an omitted description retains its existing value.

`EditProjectCommand` resolves the displayed project index, constructs a
replacement preserving the project's ID, checks for a conflicting name, and
calls `Model#setProject`. A successful edit clears the project filter.
Memberships remain valid because they reference the unchanged project ID.

`DeleteProjectCommand` resolves its index against the filtered Projects list
and calls `Model#deleteProject`. `AddressBook#removeProject` removes the project
and its memberships. Contacts and memberships belonging to other projects
remain unchanged.

### Project search and filter maintenance

`FindProjectCommandParser` splits the supplied keywords on whitespace and
creates a `ProjectNameContainsKeywordsPredicate`.

The predicate applies `StringUtil#containsWordIgnoreCase` to the project's name.
A project matches when at least one keyword matches a complete whitespace-
separated word. Descriptions and deadlines are not searched.

`FindProjectCommand` replaces the current project predicate, reports the result
count and requests the Projects tab. `listprojects` without a contact index
restores the complete project list. Neither command changes the contact filter.

Changes to the observable project collection propagate to its `FilteredList`.
An additional project-filter refresh method is therefore unnecessary for
project additions, replacements and deletions.

A project-member contact predicate depends on the separate membership
collection. Changing a membership does not itself modify the observable person
collection. `ModelManager#refreshPersonFilter` therefore temporarily removes
and reinstalls the current contact predicate after membership addition,
membership removal and project deletion.

For example, deleting the project currently selected by `listmembers` removes
its memberships and re-evaluates the contact predicate, producing an empty
Contacts list.

### Handling save failures

Before parsing and executing a command, `LogicManager#execute` copies the
address book and records both current filter predicates.

After the command executes, `LogicManager` attempts to save the address book.
If saving fails with an `IOException`, it compares the current stored data
with the snapshot.

Ordinary model equality is insufficient for this comparison:
`Person#equals` ignores person IDs, `ProjectName#equals` ignores case through
its comparison key, and `ProjectMembership#equals` ignores roles.
`LogicManager#hasSameStoredData` therefore also compares person IDs, exact
project-name strings and membership roles.

If stored data changed, `LogicManager` restores the snapshot and the previous
filtering behaviour, then throws a `CommandException` explaining that the
command was not applied.

If stored data is unchanged, the command result is returned with a save warning.
Its help flag, exit flag and list-view target are preserved. This allows
commands such as `help`, `list` and `exit` to complete despite a save failure.

When storage reports that saving is blocked following a loading failure,
commands may still execute in memory. Their results warn that saving is
disabled and session changes will be lost. The unreadable original data file
is not overwritten.

#### Design considerations

**Aspect: Where to store project memberships:**

* **Alternative 1 (current choice):** Store memberships as a separate collection in `AddressBook`.
  * Pros: Represents each association once, avoids copying entity data, and centralizes duplicate checking,
    referential integrity, and deletion cleanup.
  * Cons: Looking up a person's projects or a project's members requires filtering the membership collection.
* **Alternative 2:** Store associated projects in each `Person` and associated people in each `Project`.
  * Pros: Makes navigation from either entity direct.
  * Cons: Duplicates relationship state and requires both sides to be kept synchronized during every update.

--------------------------------------------------------------------------------------------------------------------

## **Documentation, logging, testing, dev-ops**

* [Documentation guide](Documentation.md)
* [Testing guide](Testing.md)
* [Logging guide](Logging.md)
* [DevOps guide](DevOps.md)

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Requirements**

### Product scope

**Target user profile**:

* has a need to manage a significant number of contacts
* prefers desktop apps over other types of applications
* can type fast
* prefers typing to mouse interactions
* is reasonably comfortable using CLI apps

**Value proposition**: Manage contacts faster than with a typical mouse-driven GUI application.


### User stories

Priorities: High (must have) - `* * *`, Medium (nice to have) - `* *`, Low (unlikely to have) - `*`

| Priority | As a …                                    | I want to …                 | So that I can…                                                        |
|----------|--------------------------------------------|------------------------------|------------------------------------------------------------------------|
| `* * *`  | new user                                   | see usage instructions       | refer to instructions when I forget how to use the App                 |
| `* * *`  | user                                       | add a new person             |                                                                        |
| `* * *`  | user                                       | delete a person              | remove entries that I no longer need                                   |
| `* * *`  | user                                       | find a person by name        | locate details of persons without having to go through the entire list |
| `* * *`  | returning user | retain my contacts, projects and their associations between sessions | continue where I left off without re-entering information |
| `* * *`  | university student | add a new project | organise contacts according to the projects they are involved in |
| `* * *`  | university student | associate a contact with a project | keep track of everyone involved |
| `* * *`  | university student | view all contacts for a project | see everyone involved at a glance |
| `* * *`  | university student | view all projects | quickly find the project I am working on |
| `* *`    | user                                       | hide private contact details | minimize chance of someone else seeing them by accident                |
| `*`      | user with many persons in the address book | sort persons by name         | locate a person easily                                                 |

### Use cases

(For all use cases below, the **System** is `UniTeam` and the **Actor** is the `user`, unless specified otherwise)

**Use case: Add a contact**

**MSS**

1. User request to add a contact, giving contact's name and contact details
2. UniTeam checks that the input is in the correct format
3. UniTeams checks that each given detail is valid
4. UniTeams checks that the contact does not already exist
5. UniTeams adds the contact and saves the updated data
6. Uniteams displays a confirmation message and shows the new contact in the contact list

    Use case ends.

**Extensions**

* 2a. The command format is invalid

    * 2a1. UniTeam shows an error message with the correct command format.

    Use case resumes at step 1.

* 2b. A required field is missing.

    * 2b1. UniTeam shows an error message

      Use case resumes at step 1.

* 3a. A given detail is invalid.

    * 3a1. UniTeam shows an error message

      Use case resumes at step 1.

* 4a. A contact with the same identity already exists.

    * 4a1. UniTeam shows an error message

      Use case resumes at step 1.


* 5a. UniTeam is unable to save the data.

    * 4a1. UniTeam shows an error message

      Use case ends.


**Use case: Add a project**

**MSS**

1. User requests to add a project, giving the project name, deadline and optional description
2. UniTeam checks that no existing project has the same name
3. UniTeam adds the project to the project list
4. UniTeam saves the updated data
5. UniTeam shows a confirmation message with new project's details and the project list is updated

   Use case ends.

**Extensions**

* 1a. The command format is invalid.

    * 1a1. UniTeam shows an error message

      Use case resumes at step 1.

* 1b. The project name is missing.

    * 1b1. UniTeam shows an error message

      Use case resumes at step 1.

* 2a. A project with the same name already exists.

    * 2a1. UniTeam shows an error message

      Use case resumes at step 1.

* 4a. UniTeam is unable to save the data.

    * 4a1. UniTeam shows an error message

      Use case ends.

**Use case: Assign contact to project**

**MSS**

1. User requests to view all contacts
2. UniTeam displays the contact list
3. User requests to associate a displayed contact with an existing project using `c/CONTACT_INDEX pr/PROJECT_INDEX`
4. UniTeam records and saves the association

   Use case ends.

**Extensions**

* 2a. The contact list is empty.

  Use case ends.

* 3a. The contact index is missing, empty, repeated, not a positive integer, or outside the displayed contact list.

    * 3a1. UniTeam displays an error message.

      Use case resumes at step 3.

* 3b. The project index is missing, empty, repeated, or not a positive integer.

    * 3b1. UniTeam displays an error message.

      Use case resumes at step 3.

* 3c. The supplied project index does not correspond to an existing project.

    * 3c1. UniTeam informs the user that the project index is invalid.

      Use case resumes at step 3.

* 3d. The contact is already assigned to the specified project.

    * 3d1. UniTeam informs the user that the association already exists.

      Use case resumes at step 3.

* 3e. The request contains an unsupported parameter.

    * 3e1. UniTeam displays an error message.

      Use case resumes at step 3.

* 4a. UniTeam cannot save the association.

    * 4a1. UniTeam informs the user that the association could not be saved.
    * 4a2. UniTeam leaves the existing contact and project data unchanged.

      Use case ends.

**Use case: List project contacts**

**MSS**

1. User requests to view all projects
2. UniTeam displays the project list
3. User requests to view the contacts associated with a specified project
4. UniTeam displays the associated contacts and the number of contacts found

   Use case ends.

**Extensions**

* 2a. The project list is empty.

  Use case ends.

* 3a. The project name is missing or empty, or is supplied more than once.

    * 3a1. UniTeam displays an error message.

      Use case resumes at step 3.

* 3b. The specified project does not exist.

    * 3b1. UniTeam informs the user that the project does not exist.

      Use case resumes at step 3.

* 3c. The request contains an unsupported parameter.

    * 3c1. UniTeam displays an error message.

      Use case resumes at step 3.

* 4a. The specified project has no associated contacts.

    * 4a1. UniTeam displays an empty contact list and informs the user that no contacts were found in the project.

      Use case ends.

### Non-Functional Requirements

1.  Should work on any _mainstream OS_ as long as it has Java `25` or above installed.
2.  Should be able to hold up to 1000 persons without noticeable sluggishness in performance for typical usage.
3.  A user with above average typing speed for regular English text (i.e. not code, not system admin commands) should be able to accomplish most of the tasks faster using commands than using the mouse.
4.  Should respond to any command within 2 seconds.
5.  Should work offline, without needing an internet connection.
6.  Should not lose existing data if app crashes or is closed unexpectedly.

### Glossary

* **Mainstream OS**: Windows, Linux, Unix, or macOS
* **Private contact detail**: A contact detail that is not meant to be shared with others

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Instructions for manual testing**

Given below are instructions to test the app manually.

**Note:** These instructions only provide a starting point for testers to work on;
testers are expected to do more *exploratory* testing.
</box>

### Launch and shutdown

1. Initial launch

   1. Download the JAR file and copy it into an empty folder.

   2. Double-click the JAR file.<br>
      Expected: The GUI opens with a set of sample contacts. The window size may not be optimal.

2. Saving window preferences

   1. Resize the window to an optimal size. Move the window to a different location. Close the window.

   2. Relaunch the app by double-clicking the JAR file.<br>
       Expected: The most recent window size and location are retained.

### Contacts with the same name

1. Adding same-name contacts and rejecting exact duplicates

   1. Prerequisites: Use a disposable copy of the app and data file. Execute `clear` to start with an empty address
      book; this removes the disposable copy's existing contacts, projects, and memberships.

   2. Execute `add n/Alex Tan p/91234567 e/alex@example.com a/Block 1 t/friend t/classmate`, then
      `add n/Alex Tan p/98765432 e/alex@example.com a/Block 1 t/classmate t/friend`.<br>
      Expected: Both commands succeed. The Contacts tab shows two contacts named Alex Tan with different phone
      numbers. Their saved JSON entries have distinct `id` values.

   3. Repeat the first command with the tags in reverse order.<br>
      Expected: `This contact already exists in UniTeam.` No contact is added; tag order does not distinguish contacts.

   4. Execute `find Nobody`, then repeat the first `add` command.<br>
      Expected: The filtered list is empty, but the addition is still rejected as a duplicate. Execute `list` and
      confirm that both original contacts remain with their details and IDs unchanged.

2. Editing same-name contacts

   1. Prerequisites: Continue from the preceding test with both contacts displayed in their original order.

   2. Execute `edit 2 n/Alex Tan`.<br>
      Expected: The edit succeeds although the supplied name is unchanged.

   3. Execute `edit 2 p/91234567`.<br>
      Expected: `This contact already exists in UniTeam.` The second contact still has phone number `98765432`.

   4. Execute `edit 2 n/Jamie Tan`, then `find Jamie`, then `edit 1 p/91234567 n/Alex Tan`.<br>
      Expected: The first two commands succeed. The final edit is rejected because it would match the hidden Alex
      Tan contact's complete details. Execute `list` and confirm that Jamie Tan's details and both IDs are unchanged.

   5. Execute `edit 2 n/Alex Tan a/Block 2`.<br>
      Expected: The edit succeeds. Both contacts share a name, retain their IDs, and have different details.

3. Saving same-name contacts and independent memberships

   1. Prerequisites: Continue from the editing test with both contacts displayed. Execute
      `addproject n/First project d/2027-01-01` and `addproject n/Second project d/2027-01-02`.

   2. Execute `associate c/1 pr/1` and `associate c/2 pr/2`, then record the contact IDs and membership ID pairs in
      `data/addressbook.json`.

   3. Close and restart the app, then execute `list`, `listmembers pr/First project`, and
      `listmembers pr/Second project`.<br>
      Expected: Both same-name contacts load with their saved details. Each project reports one member named
      Alex Tan. Since the member output shows names only, check the saved JSON to confirm that the first project
      still references the first contact's ID, the second project references the second contact's ID, and all
      recorded contact IDs and membership pairs are unchanged.

### Deleting a person

1. Deleting a person while all persons are being shown

   1. Prerequisites: List all persons using the `list` command, with multiple persons in the list.

   2. Test case: `delete 1`<br>
      Expected: The first contact is deleted from the list. The status message shows the deleted contact's details.

   3. Test case: `delete 0`<br>
      Expected: No person is deleted. The status message shows error details.

   4. Other incorrect delete commands to try: `delete`, `delete x`, `...` (where x is larger than the list size)<br>
      Expected: Similar to previous.

### Listing projects

1. In a fresh session, run `listprojects`.<br>
   Expected: UniTeam reports that there are no projects to list.

2. Add projects with and without descriptions using `addproject`, then run `listprojects`.<br>
   Expected: Projects are shown in insertion order with their displayed number, deadline, and description.

### Listing a contact's projects

1. Set up a disposable copy of the app in a separate folder. Run `clear` in that copy, then execute:

   ```text
   add n/Alex Querytest p/81234567 e/alex@example.com a/Block 1
   add n/Blair Querytest p/82345678 e/blair@example.com a/Block 2
   addproject n/Unrelated project d/2026-11-13
   addproject n/Alpha project d/2026-11-14 desc/First associated project
   addproject n/Beta project d/2026-11-15
   associate c/1 pr/1
   associate c/2 pr/3
   associate c/2 pr/2
   ```

2. Run `listprojects`, then `listprojects c/2`.<br>
   Expected: The first command lists all three projects. The second identifies Blair Querytest and lists Alpha as 2
   and Beta as 3, despite their reverse association order. Both results include deadlines and descriptions, with
   `No description provided` for Beta. The Contacts and Projects tabs retain all entries in their original order.

3. Run `find Blair`, then `listprojects c/1`.<br>
   Expected: The result still identifies Blair and lists projects 2 and 3. The contact search remains active.
   Run `listprojects c/2`; it reports an invalid displayed contact index because only one contact is displayed.

4. Try `listprojects c/`, `listprojects c/0`, `listprojects c/-1`, `listprojects c/abc`,
   `listprojects c/2147483648`, `listprojects c/1 c/2`, `listprojects pr/2`, and `listprojects c/1 extra`.<br>
   Expected: Each command reports an error. Run `listprojects c/1` again to confirm the same two associations remain
   and the contact search is still active.

5. Run `dissociate c/1 pr/2`, then `listprojects c/1`.<br>
   Expected: Only Beta is listed, still numbered 3. This confirms that a result's project number can be used directly
   for dissociation. Alpha remains in the Projects tab.

6. Close and restart the disposable app from the same folder, run `list`, then `listprojects c/2`.<br>
   Expected: Blair still belongs only to Beta, numbered 3. All contact and project details and IDs are retained in
   `data/addressbook.json`, and the unrelated association between Alex and project 1 is retained.

7. Run `dissociate c/2 pr/3`, then `listprojects c/2`.<br>
   Expected: The result reports that no projects are associated with Blair. The contact and all three projects remain.
   Run `associate c/2 pr/2`, then `listprojects c/2`; Alpha is listed again as project 2.

### Associating a contact with a project

1. Successful association and duplicate rejection

   1. Prerequisites: Use a disposable copy of the app in a fresh session with at least two contacts. Execute `list`
      and inspect the Contacts tab to note the first two contacts. Execute
      `addproject n/Association test project d/2026-11-13`, then open the Projects tab and confirm that the project
      is numbered 1. Use this number for `pr/`, not the UUID beside `ID:`.

   2. With the Contacts tab selected, execute `associate c/2 pr/1`.<br>
      Expected: The status message names the second displayed contact and `Association test project`.

   3. Switch to the Projects tab and repeat `associate c/2 pr/1`.<br>
      Expected: An already-associated error names both entities. No second membership is added; switching tabs
      did not change the indices.

   4. With the Projects tab still selected, execute `associate pr/1 c/1`.<br>
      Expected: Association succeeds for the first contact in the Contacts tab. One project can have multiple
      contacts, and association works from either tab.

2. Unsuccessful association attempts

   1. Prerequisites: Keep the preceding session, with only project 1 and the two successful associations.

   2. Test cases: `associate`, `associate c/2`, `associate c/ pr/1`, `associate c/0 pr/1`,
      `associate c/2 proj/1`, and `associate c/2 pr/1 x/3`.<br>
      Expected: Each command shows the `associate` usage message and creates no membership.

   3. Test cases: `associate c/1 c/2 pr/1` and `associate c/2 pr/1 pr/1`.<br>
      Expected: The duplicate-field diagnostic identifies `c/` and `pr/`, respectively. No membership is added.

   4. Test case: `associate c/10000 pr/1`<br>
      Expected: The status message reports an invalid contact index. No membership is added.

   5. Test case: `associate c/1 pr/2`<br>
      Expected: The status message reports an invalid project index. No membership is added.

   6. Repeat both successful associations from the preceding scenario, trying each from both tabs.<br>
      Expected: Both still produce already-associated errors; the rejected commands preserved existing memberships.

3. Association using a filtered contact list

   1. Create another project using `addproject n/Filtered association test d/2026-11-13`. Open the Projects tab and
      confirm that `Association test project` is still numbered 1 and `Filtered association test` is numbered 2.

   2. Select the Contacts tab and use `find` with a name keyword that matches only the second contact from the
      original list. Confirm that the matching contact is now numbered 1.

   3. Switch to Projects and back to Contacts.<br>
      Expected: Both project indices remain unchanged, and the Contacts tab still shows only the matching contact.

   4. With the Projects tab selected, execute `associate c/1 pr/2`.<br>
      Expected: The status message names that contact and `Filtered association test`; contact index 1 refers to the
      search result rather than the first contact in the full address book.

   5. Test case: `associate c/2 pr/2`<br>
      Expected: An invalid contact-index error occurs because only one contact is displayed.

   6. Select the Contacts tab and execute `list`, then `associate c/2 pr/2`.<br>
      Expected: The already-associated error identifies the same contact and project, confirming that membership
      refers to the selected contact's ID rather than its temporary search-result index.

Projects and memberships are saved with contacts after successful commands and restored when the app restarts.

### Dissociating a contact from a project

1. Successful removal while preserving other data

   1. Prerequisites: Launch a disposable copy of the app in an empty folder with no existing data file. Execute
      `list` and note the first two contacts' names and details. There must be at least two contacts. Execute
      `addproject n/Zeta dissociation test d/2026-11-13` followed by
      `addproject n/Alpha dissociation test d/2026-11-13`. Open the Projects tab and confirm that Zeta is numbered 1
      and Alpha is numbered 2, following insertion order. Use these numbers for `pr/`, not the UUIDs beside `ID:`.

   2. Execute `associate c/1 pr/1`, `associate c/1 pr/2`, and `associate c/2 pr/1`. Run
      `listmembers pr/Zeta dissociation test` and `listmembers pr/Alpha dissociation test`.<br>
      Expected: Zeta contains the first two contacts, and Alpha contains only the first contact.

   3. With the Contacts tab selected, execute `dissociate c/1 pr/1`.<br>
      Expected: The success message names the first contact and `Zeta dissociation test`.

   4. Run both `listmembers` commands again, inspect the Contacts tab, and run `listprojects`.<br>
      Expected: Zeta contains only the second contact, and Alpha still contains the first contact. Both contacts
      retain their details, and both projects retain their details, IDs, and displayed order.

2. Unsuccessful dissociation attempts

   1. Prerequisites: Keep the preceding session. The first contact belongs only to Alpha, and the second belongs
      only to Zeta. Both contacts are displayed and there are exactly two projects.

   2. Test cases: `dissociate`, `dissociate c/1`, `dissociate pr/1`, `dissociate c/ pr/1`,
      `dissociate c/0 pr/1`, `dissociate c/1 pr/abc`, `dissociate c/1 proj/1`, and
      `dissociate c/1 pr/1 x/3`.<br>
      Expected: Each command shows the `dissociate` usage message and removes no membership.

   3. Test cases: `dissociate c/1 c/2 pr/1` and `dissociate c/2 pr/1 pr/2`.<br>
      Expected: The duplicate-field diagnostic identifies `c/` and `pr/`, respectively.

   4. Test case: `dissociate c/10000 pr/1`.<br>
      Expected: An invalid contact-index error occurs.

   5. Test case: `dissociate c/1 pr/3`.<br>
      Expected: An invalid project-index error occurs.

   6. Test case: `dissociate c/1 pr/1`.<br>
      Expected: A not-associated error names the first contact and `Zeta dissociation test`.

   7. After each rejected command, run both `listmembers` commands and inspect the contact and project lists.<br>
      Expected: Zeta still contains only the second contact, Alpha still contains only the first contact, and all
      contact and project details remain unchanged.

3. Filtered contact indices and removal of a project's last member

   1. Keep the preceding session and use `find` with a name keyword that matches only the second contact from the
      original list. Confirm that this contact is now numbered 1 in the Contacts tab.

   2. Switch to the Projects tab and execute `dissociate pr/1 c/1`.<br>
      Expected: The success message names the search-result contact and `Zeta dissociation test`. The reversed
      prefix order works, the contact search remains active, and project indices remain unchanged.

   3. Run `listmembers pr/Zeta dissociation test`.<br>
      Expected: UniTeam reports no contacts associated with Zeta. The project remains in the Projects tab.

   4. Test case: `dissociate c/2 pr/2`.<br>
      Expected: An invalid contact-index error occurs because only one contact is displayed, even though the full
      contact list contains at least two contacts. `listmembers pr/Alpha dissociation test` still shows the first
      contact from the original list.

   5. Execute `list`.<br>
      Expected: Both original contacts remain, with their original details.

4. Saving and restarting, including an empty membership list

   1. Keep the preceding session. Inspect `data/addressbook.json` and record the contact and project IDs. Zeta has
      no members, and Alpha contains only the first contact.

   1. Close and restart the app from the same folder. Execute `list`, `listprojects`, and both `listmembers`
      commands.<br>
      Expected: All contacts and projects retain their details and IDs. Zeta still has no members, Alpha still
      contains the first contact, and both projects retain their displayed order.

   1. Execute `dissociate c/1 pr/1` and `dissociate c/2 pr/1`.<br>
      Expected: Both produce not-associated errors naming the selected entities; the removed memberships have not
      reappeared after restarting.

   1. Execute `dissociate c/1 pr/2`, then `listmembers pr/Alpha dissociation test`.<br>
      Expected: Removal succeeds, and Alpha has no members. No memberships remain in the app.

   1. Close and restart again, then inspect the contact and project lists, run both `listmembers` commands, and
      compare the saved IDs with the recorded values.<br>
      Expected: Both projects still have no members, and all contacts and projects retain their details and IDs.

### Listing contacts in a project

1. Use `addproject n/CS2103T Team Project d/2026-11-13`, then associate two contacts with project 1 using
   `associate c/1 pr/1` and `associate c/2 pr/1`.

2. Run `listmembers pr/CS2103T Team Project`.<br>
   Expected: Both associated contact names are displayed in contact-list order with the total count.

3. Run `listmembers pr/Unknown Project`.<br>
   Expected: UniTeam reports that the project does not exist.

4. Add a second project without associating any contacts, then run `listmembers pr/Second Project`.<br>
   Expected: UniTeam reports that no contacts are associated with that project.

5. Try `listmembers`, `listmembers pr/`, and `listmembers pr/CS2103T pr/Second Project`.<br>
   Expected: The command displays a useful format or duplicate-prefix error, and does not change project data.

### Saving data

1. Preserving contact IDs across saves and restarts

   1. Prerequisites: Use a disposable copy of the app with at least one contact. Its data file is `data/addressbook.json` by default.

   2. Edit a contact's phone number using `edit 1 p/91234567`, then inspect the saved JSON and record that contact's `id` value.

   3. Close and restart the app. Edit the same contact's phone number using `edit 1 p/92345678`, then inspect the saved JSON.<br>
      Expected: The contact's details load successfully and its `id` remains the same.

2. Loading legacy contacts without IDs

   1. In the disposable copy, close the app and remove the `id` property from one contact in the JSON file. For another contact, set `"id": null`.

   2. Restart the app, then close its window without entering a command. Inspect the JSON.<br>
      Expected: Both legacy contacts load with their original details, but the file still has the missing or null IDs. Loading and closing the window do not write back the generated IDs. Do not use the `exit` command for this step, because successful commands save the address book.

   3. Restart the app and execute `list` to trigger a save, then inspect the JSON.<br>
      Expected: Each legacy contact now has a distinct UUID in its `id` property. Existing non-null IDs are preserved. Generated IDs are written on saving, rather than immediately on loading.

   4. Record the IDs, close the window, restart the app, and execute `list` again.<br>
      Expected: All recorded IDs remain unchanged after loading and saving the migrated file.

3. Rejecting malformed stored IDs

   1. In the disposable copy, close the app and replace one contact's `id` with `"not-a-uuid"`, then restart.<br>
      Expected: The data file is rejected, a loading warning is logged, and the app starts with an empty address book.

   2. Repeat with an empty ID string or `"1-1-1-1-1"`.<br>
      Expected: The same invalid-data behavior occurs. No replacement ID is generated for a malformed value.

4. Rejecting duplicate stored IDs

   1. Prerequisites: Use a disposable copy of the app with at least two contacts with different names. Close the app and copy the first contact's `id` value into the second contact's `id` property in the JSON file.

   2. Restart the app.<br>
      Expected: The data file is rejected, a loading warning is logged, and the app starts with an empty address book. The log identifies duplicate person IDs.

   3. Repeat with one copy of the UUID written in uppercase and the other in lowercase.<br>
      Expected: The same duplicate-ID rejection occurs.

5. Dealing with missing/corrupted data files

   1. Use a disposable copy of the app and data file. Close the app and remove a closing brace from `data/addressbook.json`. Record the resulting file contents.
   2. Restart the app and execute `list`, `listprojects`, `help`, a valid `add` command, and `exit`.<br>
      Expected: The app starts with an empty address book and shows an error dialog with the absolute data file path and recovery instructions. After dismissing the dialog, a saving-disabled warning remains in the status bar, including after commands. Each successful command warns that saving is disabled and session changes will be lost. Help and exit still work. The original file contents remain unchanged. Hover over the status bar path to see its full value if it is truncated.
   3. Restart again without repairing the file and execute `list`.<br>
      Expected: Saving remains disabled and the original file contents remain unchanged.
   4. Close the app, restore a valid backup, restart, add a contact, and restart again.<br>
      Expected: The restored data and new contact load normally; saving is enabled. No loading-error dialog or saving-disabled status warning appears.
   5. Close the app and move the disposable data file elsewhere, then restart and execute `list`.<br>
      Expected: Sample contacts are loaded and saved normally; no loading-error dialog or saving-disabled warning appears. Moving the original file while the app is closed is also the intentional fresh-start procedure.
   6. Where filesystem permissions allow it, close the disposable app and deny access to the data file's parent directory while keeping the file in place. Restart the app, then restore the permissions after testing.<br>
      Expected: If file absence cannot be established, reading is attempted and the access failure produces a loading-error dialog with saving disabled, rather than loading sample contacts. The original file remains unchanged.

### Project commands, filters and member roles

1. Use a disposable copy of UniTeam in a separate folder. The following setup
   deletes that disposable copy's existing data:

   ```text
   clear
   add n/Alex Tan p/81234567 e/alex@example.com a/Block 1
   add n/Blair Tan p/82345678 e/blair@example.com a/Block 2
   addproject n/Zeta d/2027-01-01
   addproject n/Alpha d/2027-01-02
   addproject n/Beta d/2027-01-03 desc/Original description
   associate c/1 pr/2
   associate c/1 pr/3
   associate c/2 pr/3
   setrole c/1 pr/2 r/Leader
   setrole c/1 pr/3 r/Reviewer
   setrole c/2 pr/3 r/Developer
   ```

2. Execute `findproject Beta`, `listmembers pr/Alpha`, then `listprojects c/1`.<br>
   Expected: Only Beta is displayed in the Projects list, numbered 1.
   Only Alex is displayed in Contacts. The feedback lists Alpha as
   `[not displayed]` and Beta as project 1. Both filters remain active,
   and the Contacts tab remains selected.

3. Execute `setrole c/1 pr/1 r/`, then `listmembers pr/Beta`.<br>
   Expected: Alex's role in Beta is cleared, while Alex remains associated
   with Beta. Blair retains the Developer role.
   Execute `setrole c/1 pr/1 r/Reviewer` to restore Alex's Beta role.

4. Execute `editproject 1 n/Beta Revised desc/`, then
   `listmembers pr/Beta Revised`.<br>
   Expected: Beta is renamed and its description is cleared. All three
   projects are displayed because editing clears the project filter.
   Alex and Blair remain associated with the renamed project, with their
   Reviewer and Developer roles retained.

5. Close and restart UniTeam, then execute
   `listmembers pr/Beta Revised`.<br>
   Expected: The renamed project, cleared description, both memberships
   and both roles persist.

6. Execute `findproject Revised`, then `deleteproject 1`.<br>
   Expected: Beta Revised is deleted. Its active member filter now displays
   an empty Contacts list.
   Execute `list` and `listprojects`; both contacts remain, while the
   Projects list contains Zeta and Alpha.

7. Execute `listmembers pr/Alpha`.<br>
   Expected: Alex remains associated with Alpha and retains the Leader role.

8. Execute `addproject n/Beta Revised d/2027-01-03`, then
   `listmembers pr/Beta Revised`.<br>
   Expected: Recreating the deleted project's name creates a new project
   without restoring its former memberships or roles.
