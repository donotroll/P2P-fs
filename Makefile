
SRC_DIR = Source
BUILD_DIR = Build
SRC_FILES := $(wildcard $(SRC_DIR)/*.java)
CLASS_FILES := $(patsubst $(SRC_DIR)/%.java,$(BUILD_DIR)/%.class,$(SRC_FILES))

#target
all: $(CLASS_FILES)

#foreach class file
$(BUILD_DIR)/%.class: $(SRC_DIR)/%.java | $(BUILD_DIR) #optional
	javac -d $(BUILD_DIR) $< 
#% pattern match


$(BUILD_DIR):
	mkdir -p $(BUILD_DIR)

clean:
	rm -rf $(BUILD_DIR)/$(SRC_DIR)/*.class


.PHONY: all clean
