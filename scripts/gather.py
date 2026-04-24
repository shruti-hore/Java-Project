import os

base_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
out_file = os.path.join(base_dir, "forclaude.md")

with open(out_file, "w", encoding="utf-8") as f:
    f.write("# Repository Contents\n\n")

    # 1. README, agents, skills
    f.write("## 1. README.md, agents.md, skills.md\n")
    for fname in ["README.md", "agents.md", "skills.md"]:
        f.write(f"### {fname}\n")
        path = os.path.join(base_dir, "crypto_module", "Builder", fname)
        if os.path.exists(path):
            with open(path, "r", encoding="utf-8") as p:
                f.write(p.read() + "\n")
        else:
            f.write("Not found.\n")

    # Helper to find and write
    def write_files(title, extension, lang):
        f.write(f"\n## {title}\n")
        for root, dirs, files in os.walk(base_dir):
            if "target" in root.split(os.sep) or ".git" in root.split(os.sep) or "scripts" in root.split(os.sep):
                continue
            for file in files:
                if (isinstance(extension, tuple) and file.endswith(extension)) or (isinstance(extension, str) and (file.endswith(extension) or file == extension)):
                    filepath = os.path.join(root, file)
                    # We create the relative path format like "Java-Project\path\to\file"
                    rel_path = os.path.relpath(filepath, os.path.join(base_dir, ".."))
                    f.write(f"### {rel_path}\n")
                    f.write(f"```{lang}\n")
                    try:
                        with open(filepath, "r", encoding="utf-8") as file_in:
                            f.write(file_in.read() + "\n")
                    except Exception as e:
                        f.write(f"Error reading file: {e}\n")
                    f.write("```\n")

    write_files("2. pom.xml Files", "pom.xml", "xml")
    write_files("3. Java Files", ".java", "java")
    write_files("4. Flyway Migrations", ".sql", "sql")
    write_files("5. Application Properties/YAML", (".yaml", ".yml", ".properties"), "yaml")
