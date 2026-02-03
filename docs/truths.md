The core idea: models are not used raw

Neither Claude nor Codex is operating as:

“model + prompt = output”

They operate as:

model + structured context + tool contracts + execution loop

The shape and semantics of the context container strongly influence behavior.