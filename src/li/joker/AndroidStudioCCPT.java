package li.joker;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static org.objectweb.asm.Opcodes.ASM5;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

public class AndroidStudioCCPT {

    public static void main(String[] argv) throws IOException {
        String jarPath = argv[0];

        try (FileSystem fs = FileSystems.newFileSystem(URI.create("jar:" + new File(jarPath).toPath().toUri()), new HashMap<>())) {
            Path cp = fs.getPath("com/android/ide/common/repository/ResourceVisibilityLookup$Provider.class");
            byte[] cbs = Files.readAllBytes(cp);
            ClassReader classReader = new ClassReader(cbs);
            ClassWriter writer = new ClassWriter(0);
            classReader.accept(new ClassVisitor(ASM5, writer) {
                class HookMethodAdapter extends MethodVisitor {
                    HookMethodAdapter(MethodVisitor visitor) {
                        super(ASM5, visitor);
                    }

                    @Override
                    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
                        super.visitMethodInsn(opcode, owner, name, desc, itf);
                        if (opcode == INVOKEVIRTUAL
                                && owner.equals("com/android/ide/common/repository/ResourceVisibilityLookup$Provider")
                                && name.equals("get")
                                && desc.equals("(Lcom/android/builder/model/AndroidArtifact;)Lcom/android/ide/common/repository/ResourceVisibilityLookup;")) {
                            super.visitMethodInsn(INVOKESTATIC, "li/joker/AndroidStudioXMLCodeCompletionPatch", "fakeTransparentVisibility",
                                    "(Lcom/android/ide/common/repository/ResourceVisibilityLookup;)Lcom/android/ide/common/repository/ResourceVisibilityLookup;", false);
                        }
                    }
                }

                @Override
                public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                    if (name.equals("get") && desc.equals("(Lcom/android/builder/model/AndroidProject;Lcom/android/builder/model/Variant;)Lcom/android/ide/common/repository/ResourceVisibilityLookup;")) {
                        return new HookMethodAdapter(super.visitMethod(access, name, desc, signature, exceptions));
                    }
                    return super.visitMethod(access, name, desc, signature, exceptions);
                }
            }, 0);
            Files.write(cp, writer.toByteArray(), TRUNCATE_EXISTING);

            File ccPatchJar = new File(AndroidStudioXMLCodeCompletionPatch.class.getProtectionDomain().getCodeSource().getLocation().getFile());
            Files.copy(ccPatchJar.toPath(), new File(new File(jarPath).getParentFile(), ccPatchJar.getName()).toPath(), REPLACE_EXISTING);
        }
    }

}
