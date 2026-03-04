# Project Agent Rules

1. New Android UI code must use ViewBinding/DataBinding.  
   Do not introduce `findViewById` in new files when binding can be used.

2. For new list adapters, use BaseRecyclerViewAdapterHelper v4:  
   `implementation 'io.github.cymchad:BaseRecyclerViewAdapterHelper4'`
   Do not create new adapters based on Android basic `RecyclerView.Adapter`.
