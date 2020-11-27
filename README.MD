# JSON Properties FX

An editor to edit json in a property sheet - created automatically by a json schema. The editor is using JavaFX for the UI and everit-org/json-schema for the schema processing and validation.

### Open points:

Further separation of model and UI. Model should be completely UI technology agnostic.

Create UI controls lazily (only when they are displayed in the virtual flow)

Give possibility to display elements without root titledPane

All enums are treated as strings

Filter needs to accept regex.

When filtering the required and optional subparts of an object should vanish.

Add possibility to extends with custom new formats