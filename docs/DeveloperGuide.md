---
  layout: default.md
  title: "Developer Guide"
  pageNav: 3
---

# AB-3 Developer Guide

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## **Acknowledgements**

* _{List the sources of reused or adapted ideas, code, documentation, and third-party libraries here, with links to the originals.}_

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

**`Main`** (consisting of classes [`Main`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/Main.java) and [`MainApp`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/MainApp.java)) is in charge of the app launch and shut down.
* At app launch, it initializes the other components in the correct sequence, and connects them up with each other.
* At shut down, it shuts down the other components and invokes cleanup methods where necessary.

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

The **API** of this component is specified in [`Ui.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/ui/Ui.java)

<puml src="diagrams/UiClassDiagram.puml" alt="Structure of the UI Component"/>

The UI consists of a `MainWindow` and its parts, such as `CommandBox`, `ResultDisplay`, `PersonListPanel`, and `StatusBarFooter`. All of these, including `MainWindow`, inherit from the abstract `UiPart` class, which captures common behavior among classes that represent visible GUI parts.

The `UI` component uses the JavaFX UI framework. The layouts of these UI parts are defined in matching `.fxml` files in `src/main/resources/view`. For example, [`MainWindow.fxml`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/resources/view/MainWindow.fxml) specifies the layout of [`MainWindow`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/ui/MainWindow.java).

The `UI` component,

* executes user commands using the `Logic` component.
* listens for changes to `Model` data so that the UI can be updated with the modified data.
* keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
* depends on some classes in the `Model` component because it displays `Person` objects from the model.

### Logic component

**API** : [`Logic.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/logic/Logic.java)

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
**API** : [`Model.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/model/Model.java)

<puml src="diagrams/ModelClassDiagram.puml" width="650" />


The `Model` component,

* stores the address book data: all `Person` objects in a `UniquePersonList`, all `Project` objects in a
  `UniqueProjectList`, and all `ProjectMembership` objects in a `UniqueProjectMembershipList`.
* gives each `Person` a non-null, immutable internal `PersonId`, represented by a UUID, that is not displayed to users.
* stores the `Person` objects selected by the current filter, such as search results, in a separate _filtered_ list. It exposes this list as an unmodifiable `ObservableList<Person>` that the UI can observe and bind to, so the UI updates when the list changes.
* stores a `UserPrefs` object that represents the user’s preferences (currently, just the GUI settings). This is exposed to the outside as a `ReadOnlyUserPrefs` object.
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)

Each `Person` owns exactly one `PersonId`, shown by the composition in the Model diagrams. The normal `Person` constructor generates a new ID, while the reconstruction constructor accepts an existing ID. Editing replaces the person's details but preserves its ID.

IDs provide stable internal references and are separate from contact duplicate detection. `Person#isSamePerson` remains name-based, and `UniquePersonList` still uses it to reject duplicate contacts. `Person#equals` and `Person#hashCode` continue to compare the existing contact fields without including the ID; otherwise equal contacts with different IDs remain equal.

A `ProjectMembership` represents the association between one contact and one project. It stores only the contact's
`PersonId` and the project's `ProjectId`; it does not store copies of the corresponding `Person` or `Project`.
`AddressBook` maintains these associations in a `UniqueProjectMembershipList`, where each person-project ID pair may
occur at most once. A membership can be added only when both referenced entities exist in the same address book.
Deleting a person or project removes its related memberships, while editing either entity preserves its memberships
because its ID remains unchanged. Address book copies and resets include the membership collection, and callers can
inspect it only through an unmodifiable list.

`Model` exposes membership lookup and addition through `hasProjectMembership` and `addProjectMembership`.
`ModelManager` delegates these operations to `AddressBook`, which enforces membership uniqueness and valid entity
references. `AssociateCommand` resolves the displayed contact index and project list index, validates both, and
creates a membership using the selected entities' IDs. It rejects an existing pair before modifying the model and
confirms success with both names. `AddressBookParser` routes `associate` to `AssociateCommandParser`, which requires
one `c/` prefix and one `pr/` prefix, rejects a non-empty preamble, and parses both values with `ParserUtil#parseIndex`.
Syntax errors include the command usage message, while repeated prefixes retain the standard duplicate-field
diagnostic. The parser checks positive-integer syntax; the command checks the indices against the model lists.

The Contacts tab displays `Model#getFilteredPersonList()`, while the Projects tab displays `Model#getProjectList()`
through `Logic#getProjectList()` and `ProjectListPanel`. The project list is neither sorted nor filtered by the UI,
and project cards are numbered from 1 in list order. `pr/PROJECT_INDEX` uses that number, not the UUID shown beside
`ID:` on the card. Switching tabs does not change the lists or reset the contact filter. Projects and memberships
remain session-only until their persistence is implemented.


<box type="info" seamless>

**Note:** The alternative, arguably more object-oriented, design below keeps a unique list of tags in `AddressBook`, and each `Person` references tags from that list. This lets `AddressBook` maintain one `Tag` object per unique tag instead of each `Person` holding its own `Tag` objects.<br>

<puml src="diagrams/BetterModelClassDiagram.puml" width="450" />
</box>


### Storage component

**API** : [`Storage.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/storage/Storage.java)

<puml src="diagrams/StorageClassDiagram.puml" width="550" />

The `Storage` component,
* can save both address book data and user preference data in JSON format, and read them back into corresponding objects.
* is implemented by `StorageManager`, which delegates the actual JSON file access to `JsonAddressBookStorage` and `JsonUserPrefsStorage` (one class per data file).
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)

`JsonAdaptedPerson` saves each contact's `PersonId` as an `id` string in the address book JSON file and restores it when loading. A missing or null `id` is supported for legacy data and generates a new ID, which is persisted on the next save. A present ID must use the standard UUID format, with either uppercase or lowercase hexadecimal digits. Malformed IDs cause an `IllegalValueException`, which `JsonAddressBookStorage` reports as a `DataLoadingException`.

`JsonSerializableAddressBook#toModelType()` also tracks restored IDs in a `Set<PersonId>` and rejects duplicate IDs with a dedicated error message. UUIDs that differ only in capitalization represent the same ID. The existing name-based duplicate-person check runs first, and a contact is added only after both checks pass. Duplicate IDs are reported as invalid data through the same storage-loading exception path as malformed IDs.

Project and project-membership data are session-only in this iteration. `JsonSerializableAddressBook` serializes
contacts but does not serialize projects or memberships, so projects and memberships are lost when the application
terminates. There is no `JsonAdaptedProjectMembership` or `memberships` property in `addressbook.json`. Membership
persistence should be introduced only after projects and their `ProjectId` values can be reconstructed from storage;
otherwise a restored membership could not resolve its project reference.

#### Legacy migration

Loading legacy data generates IDs in memory but does not rewrite the JSON file. `LogicManager#execute()` saves the address book after every successful command, including commands such as `list` that do not change contact details. That save writes the generated IDs, and subsequent loads restore them.

`MainApp#stop()` saves user preferences only. Closing the window without executing a successful command leaves an ID-less legacy file unchanged, so its missing or null IDs are generated again on the next startup. Entering `exit` is different from closing the window: it is a successful command and follows the normal address-book save path.

Immediate migration write-back is not implemented. If IDs must remain stable across startups even when no address-book save occurs, the team must explicitly choose to add a write-back after successful legacy loading, including handling possible write failures.

### Common classes

Classes used by multiple components are in the `seedu.address.commons` package.

--------------------------------------------------------------------------------------------------------------------

## **Implementation**

This section describes some noteworthy details on how certain features are implemented.

### Project membership model

The project membership model represents the many-to-many relationship between contacts and projects: one contact can
belong to multiple projects, and one project can contain multiple contacts. Each relationship is represented by a
separate `ProjectMembership` value object containing a `PersonId`, a `ProjectId`, and a member-specific `Role`.

Using stable IDs keeps memberships valid when descriptive fields such as a contact's phone number or a project's name
change. It also avoids the stale or inconsistent data that could result from storing copies of `Person` or `Project` in
each membership. Names and list indexes are unsuitable references because names can be edited and indexes change when
the displayed list is filtered or reordered.

`AddressBook` owns the person, project, and membership collections, so it also enforces the relationship invariants. It
rejects duplicate ID pairs and memberships with unknown IDs, and removes related memberships when a person or project
is deleted. During reset, persons and projects are installed before memberships so that membership references can be
validated against the replacement data.

`Role` is an immutable value object with a 200-character, single-line constraint. `setrole` replaces the matching
membership while preserving its person and project IDs. The JSON adapter stores each role with its membership and
defaults missing role fields to an empty role for compatibility with older data. `listmembers` appends each non-empty
role to the displayed contact name.

The `listmembers` command resolves a project name to its `Project`, then asks the model for that project's contacts.
`AddressBook#getProjectMembers` matches membership IDs and returns contacts in address-book order; command logic does
not inspect or mutate the membership collection directly.

#### Design considerations

**Aspect: Where to store project memberships:**

* **Alternative 1 (current choice):** Store memberships as a separate collection in `AddressBook`.
  * Pros: Represents each association once, avoids copying entity data, and centralizes duplicate checking,
    referential integrity, and deletion cleanup.
  * Cons: Looking up a person's projects or a project's members requires filtering the membership collection.
* **Alternative 2:** Store associated projects in each `Person` and associated people in each `Project`.
  * Pros: Makes navigation from either entity direct.
  * Cons: Duplicates relationship state and requires both sides to be kept synchronized during every update.

### \[Proposed\] Undo/redo feature

#### Proposed Implementation

The proposed undo/redo mechanism is facilitated by `VersionedAddressBook`. It extends `AddressBook` with an undo/redo history, stored internally as an `addressBookStateList` and `currentStatePointer`. Additionally, it implements the following operations:

* `VersionedAddressBook#commit()` -- Saves the current address book state in its history.
* `VersionedAddressBook#undo()` -- Restores the previous address book state from its history.
* `VersionedAddressBook#redo()` -- Restores a previously undone address book state from its history.

These operations are exposed in the `Model` interface as `Model#commitAddressBook()`, `Model#undoAddressBook()` and `Model#redoAddressBook()` respectively.

Given below is an example usage scenario and how the undo/redo mechanism behaves at each step.

Step 1. The user launches the application for the first time. The `VersionedAddressBook` will be initialized with the initial address book state, and the `currentStatePointer` pointing to that single address book state.

<puml src="diagrams/UndoRedoState0.puml" alt="UndoRedoState0" />

Step 2. The user executes `delete 5` command to delete the 5th person in the address book. The `delete` command calls `Model#commitAddressBook()`, causing the modified state of the address book after the `delete 5` command executes to be saved in the `addressBookStateList`, and the `currentStatePointer` is shifted to the newly inserted address book state.

<puml src="diagrams/UndoRedoState1.puml" alt="UndoRedoState1" />

Step 3. The user executes `add n/David …​` to add a new person. The `add` command also calls `Model#commitAddressBook()`, causing another modified address book state to be saved into the `addressBookStateList`.

<puml src="diagrams/UndoRedoState2.puml" alt="UndoRedoState2" />

<box type="info" seamless>

**Note:** If a command fails its execution, it will not call `Model#commitAddressBook()`, so the address book state will not be saved into the `addressBookStateList`.
</box>

Step 4. The user now decides that adding the person was a mistake, and decides to undo that action by executing the `undo` command. The `undo` command will call `Model#undoAddressBook()`, which will shift the `currentStatePointer` once to the left, pointing it to the previous address book state, and restores the address book to that state.

<puml src="diagrams/UndoRedoState3.puml" alt="UndoRedoState3" />


<box type="info" seamless>

**Note:** If the `currentStatePointer` is at index 0, pointing to the initial AddressBook state, then there are no previous AddressBook states to restore. The `undo` command uses `Model#canUndoAddressBook()` to check if this is the case. If so, it will return an error to the user rather
than attempting to perform the undo.
</box>

The following sequence diagram shows how an undo operation goes through the `Logic` component:

<puml src="diagrams/UndoSequenceDiagram-Logic.puml" alt="UndoSequenceDiagram-Logic" />

<box type="info" seamless>

**Note:** The lifeline for `UndoCommand` should end at the destroy marker (X), but due to a limitation of PlantUML, it continues to the end of the diagram.
</box>

Similarly, how an undo operation goes through the `Model` component is shown below:

<puml src="diagrams/UndoSequenceDiagram-Model.puml" alt="UndoSequenceDiagram-Model" />

The `redo` command does the opposite — it calls `Model#redoAddressBook()`, which shifts the `currentStatePointer` once to the right, pointing to the previously undone state, and restores the address book to that state.

<box type="info" seamless>

**Note:** If the `currentStatePointer` is at index `addressBookStateList.size() - 1`, pointing to the latest address book state, then there are no undone AddressBook states to restore. The `redo` command uses `Model#canRedoAddressBook()` to check if this is the case. If so, it will return an error to the user rather than attempting to perform the redo.
</box>

Step 5. The user then decides to execute the command `list`. Commands that do not modify the address book, such as `list`, will usually not call `Model#commitAddressBook()`, `Model#undoAddressBook()` or `Model#redoAddressBook()`. Thus, the `addressBookStateList` remains unchanged.

<puml src="diagrams/UndoRedoState4.puml" alt="UndoRedoState4" />

Step 6. The user executes `clear`, which calls `Model#commitAddressBook()`. Since the `currentStatePointer` is not pointing at the end of the `addressBookStateList`, all address book states after the `currentStatePointer` will be purged. Reason: It no longer makes sense to redo the `add n/David …` command. This is the behavior that most modern desktop applications follow.

<puml src="diagrams/UndoRedoState5.puml" alt="UndoRedoState5" />

The following activity diagram summarizes what happens when a user executes a new command:

<puml src="diagrams/CommitActivityDiagram.puml" width="250" />

#### Design considerations:

**Aspect: How undo & redo execute:**

* **Alternative 1 (current choice):** Saves the entire address book.
  * Pros: Easy to implement.
  * Cons: May have performance issues in terms of memory usage.

* **Alternative 2:** Individual command knows how to undo/redo by
  itself.
  * Pros: Will use less memory (e.g. for `delete`, just save the person being deleted).
  * Cons: We must ensure that the implementation of each individual command is correct.

_{more aspects and alternatives to be added}_

### \[Proposed\] Data archiving

_{Explain here how the data archiving feature will be implemented}_


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

The requirement to retain projects and their associations between sessions is not implemented in this iteration.
Projects and memberships exist only for the active application session; only contacts are reconstructed from
`addressbook.json` after a restart.

### Use cases

(For all use cases below, the **System** is `UniTeam` and the **Actor** is the `user`, unless specified otherwise)

**Use case: Add a contact**

**MSS**

1. User request to add a contact, giving contact's name, role and contact details
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

1. User requests to add a project, giving the project name
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
4. UniTeam records the association for the current session and displays a confirmation with both names

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

<box type="info" seamless>

**Note:** These instructions only provide a starting point for testers to work on;
testers are expected to do more *exploratory* testing.
</box>

### Launch and shutdown

1. Initial launch

   1. Download the JAR file and copy it into an empty folder.

   1. Double-click the JAR file.<br>
      Expected: The GUI opens with a set of sample contacts. The window size may not be optimal.

1. Saving window preferences

   1. Resize the window to an optimal size. Move the window to a different location. Close the window.

   1. Relaunch the app by double-clicking the JAR file.<br>
       Expected: The most recent window size and location are retained.

1. _{ more test cases … }_

### Deleting a person

1. Deleting a person while all persons are being shown

   1. Prerequisites: List all persons using the `list` command, with multiple persons in the list.

   1. Test case: `delete 1`<br>
      Expected: The first contact is deleted from the list. The status message shows the deleted contact's details.

   1. Test case: `delete 0`<br>
      Expected: No person is deleted. The status message shows error details.

   1. Other incorrect delete commands to try: `delete`, `delete x`, `...` (where x is larger than the list size)<br>
      Expected: Similar to previous.

1. _{ more test cases … }_

### Listing projects

1. In a fresh session, run `listprojects`.<br>
   Expected: UniTeam reports that there are no projects to list.

1. Add projects with and without descriptions using `addproject`, then run `listprojects`.<br>
   Expected: Projects are shown in insertion order with their displayed number, deadline, and description.

### Associating a contact with a project

1. Successful association and duplicate rejection

   1. Prerequisites: Use a disposable copy of the app in a fresh session with at least two contacts. Execute `list`
      and inspect the Contacts tab to note the first two contacts. Execute
      `addproject n/Association test project d/2026-11-13`, then open the Projects tab and confirm that the project
      is numbered 1. Use this number for `pr/`, not the UUID beside `ID:`. Projects are not loaded across sessions
      in this iteration.

   1. With the Contacts tab selected, execute `associate c/2 pr/1`.<br>
      Expected: The status message names the second displayed contact and `Association test project`.

   1. Switch to the Projects tab and repeat `associate c/2 pr/1`.<br>
      Expected: An already-associated error names both entities. No second membership is added; switching tabs
      did not change the indices.

   1. With the Projects tab still selected, execute `associate pr/1 c/1`.<br>
      Expected: Association succeeds for the first contact in the Contacts tab. One project can have multiple
      contacts, and association works from either tab.

1. Unsuccessful association attempts

   1. Prerequisites: Keep the preceding session, with only project 1 and the two successful associations.

   1. Test cases: `associate`, `associate c/2`, `associate c/ pr/1`, `associate c/0 pr/1`,
      `associate c/2 proj/1`, and `associate c/2 pr/1 x/3`.<br>
      Expected: Each command shows the `associate` usage message and creates no membership.

   1. Test cases: `associate c/1 c/2 pr/1` and `associate c/2 pr/1 pr/1`.<br>
      Expected: The duplicate-field diagnostic identifies `c/` and `pr/`, respectively. No membership is added.

   1. Test case: `associate c/10000 pr/1`<br>
      Expected: The status message reports an invalid contact index. No membership is added.

   1. Test case: `associate c/1 pr/2`<br>
      Expected: The status message reports an invalid project index. No membership is added.

   1. Repeat both successful associations from the preceding scenario, trying each from both tabs.<br>
      Expected: Both still produce already-associated errors; the rejected commands preserved existing memberships.

1. Association using a filtered contact list

   1. Create another project using `addproject n/Filtered association test d/2026-11-13`. Open the Projects tab and
      confirm that `Association test project` is still numbered 1 and `Filtered association test` is numbered 2.

   1. Select the Contacts tab and use `find` with a name keyword that matches only the second contact from the
      original list. Confirm that the matching contact is now numbered 1.

   1. Switch to Projects and back to Contacts.<br>
      Expected: Both project indices remain unchanged, and the Contacts tab still shows only the matching contact.

   1. With the Projects tab selected, execute `associate c/1 pr/2`.<br>
      Expected: The status message names that contact and `Filtered association test`; contact index 1 refers to the
      search result rather than the first contact in the full address book.

   1. Test case: `associate c/2 pr/2`<br>
      Expected: An invalid contact-index error occurs because only one contact is displayed.

   1. Select the Contacts tab and execute `list`, then `associate c/2 pr/2`.<br>
      Expected: The already-associated error identifies the same contact and project, confirming that membership
      refers to the selected contact's ID rather than its temporary search-result index.

Projects and memberships are not serialized in this iteration, so these associations do not survive a restart.

### Listing contacts in a project

1. Use `addproject n/CS2103T Team Project d/2026-11-13`, then associate two contacts with project 1 using
   `associate c/1 pr/1` and `associate c/2 pr/1`.

1. Run `listmembers pr/CS2103T Team Project`.<br>
   Expected: Both associated contact names are displayed in contact-list order with the total count.

1. Run `listmembers pr/Unknown Project`.<br>
   Expected: UniTeam reports that the project does not exist.

1. Add a second project without associating any contacts, then run `listmembers pr/Second Project`.<br>
   Expected: UniTeam reports that no contacts are associated with that project.

1. Try `listmembers`, `listmembers pr/`, and `listmembers pr/CS2103T pr/Second Project`.<br>
   Expected: The command displays a useful format or duplicate-prefix error, and does not change project data.

### Saving data

1. Preserving contact IDs across saves and restarts

   1. Prerequisites: Use a disposable copy of the app with at least one contact. Its data file is `data/addressbook.json` by default.

   1. Edit a contact's phone number using `edit 1 p/91234567`, then inspect the saved JSON and record that contact's `id` value.

   1. Close and restart the app. Edit the same contact's phone number using `edit 1 p/92345678`, then inspect the saved JSON.<br>
      Expected: The contact's details load successfully and its `id` remains the same.

1. Loading legacy contacts without IDs

   1. In the disposable copy, close the app and remove the `id` property from one contact in the JSON file. For another contact, set `"id": null`.

   1. Restart the app, then close its window without entering a command. Inspect the JSON.<br>
      Expected: Both legacy contacts load with their original details, but the file still has the missing or null IDs. Loading and closing the window do not write back the generated IDs. Do not use the `exit` command for this step, because successful commands save the address book.

   1. Restart the app and execute `list` to trigger a save, then inspect the JSON.<br>
      Expected: Each legacy contact now has a distinct UUID in its `id` property. Existing non-null IDs are preserved. Generated IDs are written on saving, rather than immediately on loading.

   1. Record the IDs, close the window, restart the app, and execute `list` again.<br>
      Expected: All recorded IDs remain unchanged after loading and saving the migrated file.

1. Rejecting malformed stored IDs

   1. In the disposable copy, close the app and replace one contact's `id` with `"not-a-uuid"`, then restart.<br>
      Expected: The data file is rejected, a loading warning is logged, and the app starts with an empty address book.

   1. Repeat with an empty ID string or `"1-1-1-1-1"`.<br>
      Expected: The same invalid-data behavior occurs. No replacement ID is generated for a malformed value.

1. Rejecting duplicate stored IDs

   1. Prerequisites: Use a disposable copy of the app with at least two contacts with different names. Close the app and copy the first contact's `id` value into the second contact's `id` property in the JSON file.

   1. Restart the app.<br>
      Expected: The data file is rejected, a loading warning is logged, and the app starts with an empty address book. The log identifies duplicate person IDs.

   1. Repeat with one copy of the UUID written in uppercase and the other in lowercase.<br>
      Expected: The same duplicate-ID rejection occurs.

1. Dealing with missing/corrupted data files

   1. _{Explain how to simulate missing or corrupted data files and state the expected behavior.}_

1. _{ more test cases … }_
