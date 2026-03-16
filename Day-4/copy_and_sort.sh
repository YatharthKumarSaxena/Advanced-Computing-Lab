#!/bin/bash

# Assign parameters to readable variable names
SRC_DIR="$1"
DEST_DIR="$2"
ORDER="$3"

# 1. Validate the number of arguments
if [ "$#" -ne 3 ]; then
    echo "Usage: $0 <sourceFolder> <destFolder> <asc|desc>"
    exit 1
fi

# 2. Check if the source directory exists
if [ ! -d "$SRC_DIR" ]; then
    echo "Error: Source folder '$SRC_DIR' does not exist."
    exit 1
fi

# 3. Validate the order argument and set the sort flag
if [ "$ORDER" = "asc" ]; then
    SORT_FLAG=""
elif [ "$ORDER" = "desc" ]; then
    SORT_FLAG="-r"
else
    echo "Error: Order must be 'asc' or 'desc'. You provided: '$ORDER'"
    exit 1
fi

# 4. Create the destination directory
mkdir -p "$DEST_DIR"

echo "Starting copy process..."
echo "------------------------------------------------"

# 5. Find direct files, sort them, copy them, and print to the log
find "$SRC_DIR" -maxdepth 1 -type f -exec basename {} \; | sort $SORT_FLAG | while IFS= read -r file; do
    
    # Generate the current timestamp
    TIMESTAMP=$(date +"%Y-%m-%d %H:%M:%S")
    # Generate a filename-safe version (replacing spaces/colons with dashes or underscores)
    FILE_TS=$(date +"%Y-%m-%d_%H-%M-%S")

    # Split filename and extension
    FILENAME="${file%.*}"
    EXTENSION="${file##*.}"

    # Handle files without extensions
    if [ "$FILENAME" = "$EXTENSION" ]; then
        NEW_NAME="${FILENAME}_${FILE_TS}"
    else
        NEW_NAME="${FILENAME}_${FILE_TS}.${EXTENSION}"
    fi
    
    # Copy the file to the destination with the new name
    cp "$SRC_DIR/$file" "$DEST_DIR/$NEW_NAME"
    
    # Print the log output
    echo "[$TIMESTAMP] Copied: $file -> $NEW_NAME"
    
done

echo "------------------------------------------------"
echo "Process complete! Files have been copied to '$DEST_DIR'."