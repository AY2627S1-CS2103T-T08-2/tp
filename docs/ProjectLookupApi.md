# Project lookup API

Project reads are exposed by `Logic` and `Model`; callers do not need to depend on the UI.

* `getProjects()` returns all projects ordered by numeric ID.
* `getContactsForProject(projectId)` returns all matching contacts.
* `getProjectsForContact(contactName)` returns all projects associated with that contact.

These lookups are read-only. They do not create projects or change assignments. Unknown IDs and
names return an empty list.

The address-book JSON stores projects and links alongside contacts:

```json
{
  "persons": [
    { "name": "Alex Tan", "phone": "91234567", "email": "alex@example.com",
      "address": "Singapore", "tags": [] }
  ],
  "projects": [
    { "id": 1, "name": "UniTeam" }
  ],
  "projectContacts": [
    { "projectId": 1, "contactName": "Alex Tan" }
  ]
}
```

Each `projectContacts` entry is one edge in the many-to-many relationship. A contact can have
entries for multiple projects, and a project can have entries for multiple contacts. In this
AddressBook model, contacts are keyed by their unique name because `Person` does not yet define a
separate ID. The loader rejects duplicate project IDs, duplicate links, or links to missing records.
