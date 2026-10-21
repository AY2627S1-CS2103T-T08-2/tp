---
  layout: default.md
  title: "User Guide"
  pageNav: 3
---

# UniTeam User Guide

UniTeam is a **desktop application for managing academic contacts and projects separate from personal phone contacts 
and organised by involvement in projects, optimised for use through a Command Line Interface (CLI)** 
while retaining the benefits of a Graphical User Interface (GUI). If you type quickly, UniTeam can help you manage 
contacts faster than traditional GUI applications.

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## Quick start

1. Ensure that Java `25` or later is installed on your computer.<br>
   **Mac users:** Ensure you have the precise JDK version prescribed [here](https://se-education.org/guides/tutorials/javaInstallationMac.html).

1. Download the latest `.jar` file from [here](https://github.com/AY2627S1-CS2103T-T08-2/tp/releases).

1. Copy the file to the folder you want to use as the _home folder_ for UniTeam.

1. Open a terminal, `cd` to the folder containing the JAR file, and run `java -jar uniteam.jar`.<br>
   A GUI similar to the one below should appear in a few seconds. Note how the app contains some sample data.<br>
   ![Ui](images/Ui.png)

1. Type a command in the command box and press Enter to execute it. For example, type **`help`** and press Enter to open the help window.<br>
   Some example commands you can try:

   * `list` : Lists all contacts.

   * `add n/John Doe p/98765432 e/johnd@example.com a/John street, block 123, #01-01` : Adds a contact named `John 
   Doe` to UniTeam.

   * `delete 3` : Deletes the 3rd contact shown in the current list.

   * `clear` : Deletes all contacts.

   * `exit` : Exits the app.

1. Refer to the [Features](#features) section below for details of each command.

--------------------------------------------------------------------------------------------------------------------

## Features

<box type="info" seamless>

**Notes about the command format:**<br>

* Words in `UPPER_CASE` are the parameters to be supplied by the user.<br>
  For example, in `add n/NAME`, replace `NAME` with a value such as `John Doe`.

* Items in square brackets are optional.<br>
  For example, `n/NAME [t/TAG]` can be used as `n/John Doe t/friend` or as `n/John Doe`.

* Items followed by `...` can appear zero or more times.<br>
  For example, `[t/TAG]... ` may be omitted, or written as `t/friend` or `t/friend t/family`.

* Parameters can be in any order.<br>
  For example, if the command specifies `n/NAME p/PHONE_NUMBER`, `p/PHONE_NUMBER n/NAME` is also acceptable.

* Commands that take no parameters (`help`, `list`, `listprojects`, `exit`, and `clear`) reject extra arguments.<br>
  For example, `help 123` reports an invalid command format and shows the usage for `help`.
  Leading and trailing whitespace is allowed.

* If you are using a PDF version of this document, be careful when copying and pasting commands that span multiple lines as space characters surrounding line-breaks may be omitted when copied over to the application.
</box>

### Viewing help: `help`

Shows a message explaining how to access the help page.

![help message](images/helpMessage.png)

Format: `help`


### Adding a contact: `add`

Adds a contact to UniTeam.

Format: `add n/NAME p/PHONE_NUMBER e/EMAIL a/ADDRESS [t/TAG]... `

<box type="tip" seamless>

**Tip:** A contact can have any number of tags, including zero.
</box>

Examples:
* `add n/John Doe p/98765432 e/johnd@example.com a/John street, block 123, #01-01`
* `add n/Betsy Crowe t/friend e/betsycrowe@example.com a/Newgate Prison p/1234567 t/criminal`

### Adding a project: `addproject`

Adds a project to UniTeam.

Format: `addproject n/PROJECT_NAME d/YYYY-MM-DD [desc/DESCRIPTION]`


### Listing all contacts: `list`

Shows a list of all contacts in UniTeam.

Format: `list`

### Listing all projects: `listprojects`

Lists projects in the order they were added, including each project's deadline and description.

Format: `listprojects`

If no projects exist, UniTeam reports that there are no projects to list.

### Editing a contact: `edit`

Edits an existing contact in UniTeam.

Format: `edit INDEX [n/NAME] [p/PHONE] [e/EMAIL] [a/ADDRESS] [t/TAG]... `

* Edits the contact at the specified `INDEX`. The index refers to the index number shown in the displayed contact list. 
  The index **must be a positive integer** 1, 2, 3, ...
* At least one of the optional fields must be provided.
* Existing values will be updated to the input values.
* When editing tags, all of the contact's existing tags are removed; adding tags is not cumulative.
* To remove all of a contact's tags, enter `t/` without a tag after it.

Examples:
*  `edit 1 p/91234567 e/johndoe@example.com` Edits the phone number and email address of the 1st contact to be 
   `91234567` 
   and `johndoe@example.com` respectively.
*  `edit 2 n/Betsy Crower t/` Edits the name of the 2nd contact to be `Betsy Crower` and clears all existing tags.

### Locating contacts by name: `find`

Finds contacts whose names contain any of the given keywords.

Format: `find KEYWORD [MORE_KEYWORDS]`

* The search is case-insensitive; for example, `hans` matches `Hans`.
* Keyword order does not matter; for example, `Hans Bo` matches `Bo Hans`.
* The search considers only names.
* Only full words match; for example, `Han` does not match `Hans`.
* Contacts matching at least one keyword are returned (an `OR` search); for example, `Hans Bo` returns `Hans Gruber` 
  and `Bo Yang`.

Examples:
* `find John` returns `john` and `John Doe`
* `find alex david` returns `Alex Yeoh`, `David Li`<br>
  ![result for 'find alex david'](images/findAlexDavidResult.png)

### Deleting a contact: `delete`

Deletes the specified contact from UniTeam.

Format: `delete INDEX`

* Deletes the contact at the specified `INDEX`.
* The index refers to the index number shown in the displayed contact list.
* The index **must be a positive integer** 1, 2, 3, ...

Examples:
* `list` followed by `delete 2` deletes the 2nd contact in UniTeam .
* `find Betsy` followed by `delete 1` deletes the 1st contact in the results of the `find` command.

### Associating a contact with a project: `associate`

Records that a contact is involved in an existing project.

Format: `associate c/CONTACT_INDEX pr/PROJECT_INDEX`

* `CONTACT_INDEX` refers to the numbered contact in the Contacts tab, including results from `find`.
* `PROJECT_INDEX` refers to the numbered project in the Projects tab, not the UUID shown beside `ID:`.
* Both indices must be positive integers. Each prefix must appear exactly once; their order does not matter.
* An existing contact-project pair cannot be associated again. A contact can belong to multiple projects, and a
  project can involve multiple contacts.
* The success message identifies the contact and project. Invalid syntax, out-of-range indices, and duplicate
  associations produce an error and leave existing associations unchanged.

Projects are numbered in the Projects tab starting at 1, in the order they are added with `addproject`.
Switching tabs does not change either list's indices or reset the contact search results.

Example, in a new session with at least two contacts:

1. `list` shows all contacts in the Contacts tab; note the second contact's name.
2. `addproject n/CS2103T Team Project d/2026-11-13` creates project 1.
3. Open the Projects tab and confirm that `CS2103T Team Project` is numbered 1.
4. `associate c/2 pr/1` associates the second contact in the Contacts tab with that project.

After `find Betsy`, use `associate c/1 pr/1` to associate the first search result with project 1 instead.

### Dissociating a contact from a project: `dissociate`

Removes an existing association between a contact and a project while retaining their information.

Format: `dissociate c/CONTACT_INDEX pr/PROJECT_INDEX`

* `CONTACT_INDEX` refers to the numbered contact in the Contacts tab, including results from `find`.
* `PROJECT_INDEX` refers to the numbered project in the Projects tab, not the UUID shown beside `ID:`.
* Both indices must be positive integers. Each prefix must appear exactly once; their order does not matter.
* Only the selected association is removed. The contact, project, and all other associations remain unchanged.
* The success message identifies the contact and project. Invalid syntax, out-of-range indices, and a pair with no
  existing association produce an error without changing the data.
* Successful removal is saved automatically and remains in effect after restarting UniTeam.

Switching tabs does not change either list's indices or reset the contact search results.

Example, with at least two displayed contacts and project 1 named `CS2103T Team Project`:

1. `associate c/2 pr/1` creates the association if it does not already exist.
2. `dissociate c/2 pr/1` removes that association while retaining the contact and project.
3. `listmembers pr/CS2103T Team Project` confirms that the contact is no longer a member; other members remain.

After `find Betsy`, use `dissociate c/1 pr/1` to remove the first search result's association with project 1.
Repeating dissociation for the same pair reports that the contact is not associated with the project.

### Listing project members: `listmembers`

Lists the contacts associated with a project, in the order they appear in the contact list.

Format: `listmembers pr/PROJECT_NAME`

Example:

1. Create a project with `addproject n/CS2103T Team Project d/2026-11-13`.
2. Associate a contact with it, for example `associate c/1 pr/1`.
3. Run `listmembers pr/CS2103T Team Project` to see the associated contact names.

If the project exists but has no associated contacts, UniTeam reports that no contacts are associated with it.
If no project has the specified name, UniTeam reports that the project does not exist. Project names are matched
without regard to letter case and with whitespace normalised.

### Setting a member's role: `setrole`

Sets or updates a short description of a contact's task allotment in a project. The contact must already be associated
with the project. A role can contain up to 200 characters and cannot contain line breaks.

Format: `setrole c/CONTACT_INDEX pr/PROJECT_INDEX r/ROLE`

Example: `setrole c/2 pr/1 r/Implement the parser`

`listmembers` displays a non-empty role beside the contact's name. An empty role is shown as just the contact's name.

### Clearing all entries: `clear`

Clears all contacts, projects and associations from UniTeam.

Format: `clear`

### Exiting the program: `exit`

Exits the program.

Format: `exit`

### Saving the data

UniTeam automatically saves contacts, projects and associations after every successful command. You do not need to 
save manually.

### Editing the data file

UniTeam data is saved automatically as a JSON file `[JAR file location]/data/addressbook.json`. Advanced users are 
welcome to update data directly by editing that data file.

<box type="warning" seamless>

**Caution:**
If your changes make the data file invalid, UniTeam starts with an empty address book at the next run. A startup error 
dialog shows the absolute path of the affected file and recovery instructions. Saving is disabled for that session to preserve the original file, and the status bar keeps a saving-disabled warning visible. Successful commands also display a warning, and changes made in that session are lost when the application closes. Close the application, copy the original file somewhere safe, then repair it or restore a valid backup and restart to resume saving. We recommend backing up the file before editing it.<br>
An existing file that cannot be read also triggers loading-error handling. Only a file confirmed to be missing follows the normal first-launch behavior.<br>
To intentionally start fresh, close the application and move the original data file somewhere safe. Restart the application; it will follow the normal first-launch behavior and populate sample contacts.<br>
Furthermore, certain edits can cause UniTeam to behave in unexpected ways (e.g., if a value entered is outside of the 
acceptable range). Therefore, edit the data file only if you are confident that you can update it correctly.
</box>

_Details coming soon ..._

--------------------------------------------------------------------------------------------------------------------

## FAQ

**Q**: How do I transfer my data to another computer?<br>
**A**: Install the app on the other computer and overwrite the data file it creates with the data file from your 
previous UniTeam home folder.

--------------------------------------------------------------------------------------------------------------------

## Known issues

1. **When using multiple screens**, if you move the application to a secondary screen, and later switch to using only the primary screen, the GUI will open off-screen. The remedy is to delete the `preferences.json` file created by the application before running the application again.
2. **If you minimize the Help Window** and then run the `help` command (or use the `Help` menu, or the keyboard shortcut `F1`) again, the original Help Window will remain minimized, and no new Help Window will appear. The remedy is to manually restore the minimized Help Window.

--------------------------------------------------------------------------------------------------------------------

## Command summary

Action     | Format, Examples
-----------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------
**Add**    | `add n/NAME p/PHONE_NUMBER e/EMAIL a/ADDRESS [t/TAG]... ` <br> e.g., `add n/James Ho p/22224444 e/jamesho@example.com a/123, Clementi Rd, 1234665 t/friend t/colleague`
**Associate** | `associate c/CONTACT_INDEX pr/PROJECT_INDEX`<br> e.g., `associate c/2 pr/1`
**Clear**  | `clear`
**Delete** | `delete INDEX`<br> e.g., `delete 3`
**Dissociate** | `dissociate c/CONTACT_INDEX pr/PROJECT_INDEX`<br> e.g., `dissociate c/2 pr/1`
**Edit**   | `edit INDEX [n/NAME] [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [t/TAG]... `<br> e.g.,`edit 2 n/James Lee e/jameslee@example.com`
**Find**   | `find KEYWORD [MORE_KEYWORDS]`<br> e.g., `find James Jake`
**List**   | `list`
**List projects** | `listprojects`
**List members** | `listmembers pr/PROJECT_NAME`<br> e.g., `listmembers pr/CS2103T Team Project`
**Help**   | `help`
