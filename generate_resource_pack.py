import sys, os, time, shutil, json, zipfile

# A script that extracts the assets and generated assets folders of a mod
# and creates a barebones template resource pack for it.
# Should work on just about any mod, though you do need to be in the dev
# environment to get the generated assets.

ASSETS_PATH = os.path.join("src", "main", "resources", "assets")
GENERATED_ASSETS_PATH = os.path.join("src", "main", "generated", "assets")
DEFAULT_OUT_PATH = os.path.join("out")
DEFAULT_NAME = "resource_pack"

# https://minecraft.wiki/w/Pack_format
PACK_VERSION = 26

config = {
  "include_generated_assets": False,
  "outPath": DEFAULT_OUT_PATH,
  "name": DEFAULT_NAME
}

def ensure_directory_exists(dirPath):
    if not os.path.isdir(dirPath):
        os.makedirs(dirPath)
        print("Creating", dirPath, "since it does not exist")

def clear_output(outPath):
    # Remove zip files only
    for f in os.listdir(outPath):
        pathToRemove = os.path.join(outPath, f)
        isDir = os.path.isdir(pathToRemove) and f == config["name"]
        isZip = os.path.isfile(pathToRemove) and f == config["name"] + ".zip"
        if isZip:
            try:
                os.remove(pathToRemove)
            except:
                print("Failed to remove zip file", pathToRemove)
        elif isDir:
          try:
            for root, dirs, files in os.walk(pathToRemove):
              for f1 in files:
                  os.unlink(os.path.join(root, f1))
              for d1 in dirs:
                  shutil.rmtree(os.path.join(root, d1))
          except:
            print("Failed to clear output folder", pathToRemove)

def copy_resource_pack_files(sourcePath, outPath):
  shutil.copytree(sourcePath, outPath, dirs_exist_ok=True)

def zip_resource_pack_files(uncompressedPath, outPath):
  zipObj = zipfile.ZipFile(os.path.join(outPath, config["name"] + ".zip"), "w", zipfile.ZIP_DEFLATED)
  for root, dirs, files in os.walk(uncompressedPath, topdown=True):
    for file in files:
      sourcePath = os.path.join(root, file)
      relPath = os.path.relpath(sourcePath, uncompressedPath)
      zipObj.write(sourcePath, relPath)
  zipObj.close()
    
def main():
  global config
  args = sys.argv[1:]

  # Process flags
  if "-g" in args:
    config["include_generated_assets"] = True
    args.remove("-g")

  # Process positional arguments
  if len(args) >= 1:
    config["outPath"] = args[0]
  if len(args) >= 2:
    config["name"] = args[1]

  start = time.time()
  outPath = config["outPath"]
  
  # Cleanup
  ensure_directory_exists(outPath)
  clear_output(outPath)
  
  # Copy over assets
  resourcePackPath = os.path.join(outPath, config["name"])
  assetsPath = os.path.join(resourcePackPath, "assets")
  ensure_directory_exists(resourcePackPath)
  if config["include_generated_assets"]:
    if os.path.isdir(GENERATED_ASSETS_PATH):
      copy_resource_pack_files(GENERATED_ASSETS_PATH, assetsPath)
    else:
      print("Unable to locate generated assets directory")
  if os.path.isdir(ASSETS_PATH):
    copy_resource_pack_files(ASSETS_PATH, assetsPath)
  else:
    print("Unable to locate assets directory")
  
  # Add pack.mcmeta file
  packMcMeta = {
    "pack": {
      "pack_format": PACK_VERSION,
      "description": "Generated mod resource pack" 
    }
  }
  packMcMetaPath = os.path.join(resourcePackPath, "pack.mcmeta")
  with open(packMcMetaPath, 'w') as jsonFile:
    jsonFile.write(json.dumps(packMcMeta, indent=4))
  
  # Zip
  zip_resource_pack_files(resourcePackPath, outPath)
  
  end = time.time()
  print("Finished in", round((end - start), 4), "seconds")

if __name__ == "__main__":
  main()
