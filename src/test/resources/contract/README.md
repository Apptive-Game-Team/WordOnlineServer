# Game session contract fixtures

These files are the wire format of `POST /api/server/game-sessions`, shared with
`Apptive-Game-Team/WordOnlineMatching`. The same two files exist there under
`src/test/resources/contract/` with identical content.

- `create-session-request.json` - what the lobby sends. Verified here by deserializing it
  into this server's `CreateSessionRequest`; verified there by serializing the lobby's own
  `CreateSessionRequest`.
- `session-ready-response.json` - what this server answers. Verified here by serializing
  `SessionReadyResponse`; verified there by deserializing into the lobby's own
  `SessionReadyResponse`.

Changing a field name or the nesting on one side alone makes the other side's test fail,
which is the point: without these, both repositories' suites stay green through a broken
contract. A mismatch does not surface as an exception at runtime either - the lobby reads
it as a refusal and fails over, so every candidate is rejected and matching returns 503
with nothing in the logs pointing at the cause.

When the contract changes, update both copies in the same pull request pair and say so in
each body.
