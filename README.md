# Format Registry Indexer

Simple iRODS indexing framework plugin to call out to a remote format registry server and use the identification component. Integrating this with a larger iRODS identification pipeline would probably rely on local identification components first (JHOVE, Droid, libmagic, etc) and only invoke this if those failed. Additionally, it would be possible to rewrite this to incorporate a snapshot of the format registry identifier locally and not require the file to be sent over the network to the format registry site.
