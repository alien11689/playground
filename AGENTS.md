<!-- OPENSPEC:START -->
# OpenSpec Instructions

These instructions are for AI assistants working in this project.

Always open `@/openspec/AGENTS.md` when the request:
- Mentions planning or proposals (words like proposal, spec, change, plan)
- Introduces new capabilities, breaking changes, architecture shifts, or big performance/security work
- Sounds ambiguous and you need the authoritative spec before coding

Use `@/openspec/AGENTS.md` to learn:
- How to create and apply change proposals
- Spec format and conventions
- Project structure and guidelines

Keep this managed block so 'openspec update' can refresh the instructions.

<!-- OPENSPEC:END -->

# AI Assistant Rules

## File Management Rule
**ALWAYS add new EMPTY files to git immediately after creation**

When you create any new file (specifications, code, documentation, etc.), you MUST:
1. Create the file as empty
2. Run `git add <file-path>` to track the empty file
3. Then add content to the file

**NEVER add file content changes to git staging area**
**NEVER run `git add` on existing files with changes**
**NEVER commit or push without explicit user request**

## Bash Command Restriction Rule
**NEVER execute the `rm` command without explicit user permission**

When using bash commands:
- ALWAYS check if the command involves file deletion
- If a command uses `rm`, request explicit user confirmation before executing
- Log any attempted `rm` usage for transparency

