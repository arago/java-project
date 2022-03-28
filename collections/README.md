# Collections

This is a library containing useful collection classes.

Included:

`ExpiringStore`

A Collection that handles items via their id. Each item has an expiry date after which it will be automatically removed
from the collection.

`ExpiringRetryStore`

Same as above, but items will be removed after a certain amount of calls to a getter method.

Both classes above are very useful when asynchronously handling messages which can expire.