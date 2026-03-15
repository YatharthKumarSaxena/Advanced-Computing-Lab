#!/bin/bash

SRC_DIR="$1"
DEST_DIR="$2"
ORDER="$3"

# 1. Validation
if [ "$#" -ne 3 ]; then
    echo "Usage: $0 <sourceFolder> <destFolder> <asc|desc>"
    exit 1
fi

# 2. Check source
if [ ! -d "$SRC_DIR" ]; then
    echo "Error: Source folder '$SRC_DIR' does not exist."
    exit 1
fi

# 3. Sort logic
if [ "$ORDER" = "asc" ]; then
    SORT_FLAG=""
elif [ "$ORDER" = "desc" ]; then
    SORT_FLAG="-r"
else
    echo "Error: Order must be 'asc' or 'desc'."
    exit 1
fi

# 4. Create destination (Bina purana data delete kiye)
mkdir -p "$DEST_DIR"

echo "Starting copy process..."
echo "------------------------------------------------"

COUNTER=1

# 5. Process files
find "$SRC_DIR" -maxdepth 1 -type f -exec basename {} \; | sort $SORT_FLAG | while IFS= read -r file; do
    
    if [ "$ORDER" = "desc" ]; then
        NEW_FILENAME="${COUNTER}_${file}"
        ((COUNTER++))
    else
        NEW_FILENAME="$file"
    fi
    
    # Overwrite karega par baaki files ko delete nahi karega
    cp "$SRC_DIR/$file" "$DEST_DIR/$NEW_FILENAME"
    
    TIMESTAMP=$(date +"%Y-%m-%d %H:%M:%S")
    
    if [ "$ORDER" = "desc" ]; then
        echo "[$TIMESTAMP] Copied: $file -> $NEW_FILENAME"
    else
        echo "[$TIMESTAMP] Copied: $file"
    fi
done

echo "------------------------------------------------"
echo "Process complete!"