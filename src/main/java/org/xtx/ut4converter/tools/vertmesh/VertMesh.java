package org.xtx.ut4converter.tools.vertmesh;

import org.apache.commons.io.FilenameUtils;
import org.xtx.ut4converter.tools.objmesh.ObjStaticMesh;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.List;


/**
 * Class for reading vertmesh files ( *d.3d files)
 * 
 * @author XtremeXp
 *
 */
public class VertMesh {

	/**
	 * Order how bytes are read for vert mesh files
	 */
	private static final ByteOrder BYTE_ORDER_LE = ByteOrder.LITTLE_ENDIAN;
	private File vertMeshFile;
	private FJSDataHeader dataHead;
	private List<FJSMeshTri> faces;

	public VertMesh() {
		init();
	}

	public VertMesh(File vertMeshFile) {
		this.vertMeshFile = vertMeshFile;
		init();
	}

	private void init() {
		dataHead = new FJSDataHeader();
		faces = new LinkedList<>();
	}

	
	
	public FJSDataHeader getDataHead() {
		return dataHead;
	}

	public List<FJSMeshTri> getFaces() {
		return faces;
	}

	public void read() throws Exception {

		if (vertMeshFile == null || !vertMeshFile.exists()) {
			return;
		}


		ByteBuffer buffer ;
		
		try (final FileInputStream fis = new FileInputStream(vertMeshFile);
			 final FileChannel inChannel = fis.getChannel()) {

			buffer = inChannel.map(FileChannel.MapMode.READ_ONLY, 0, (int) vertMeshFile.length());
			buffer.order(BYTE_ORDER_LE);
			dataHead.read(buffer);
			
			for(int i=0; i < dataHead.getNumPolys() ; i++){
				FJSMeshTri face = new FJSMeshTri();
				face.read(buffer);
				//System.out.println(face.toString());
				faces.add(face);
			}
		}
	}

	public static void main(String args[]) {

		//File f = new File("E:\\TEMP\\bandage_d.3d");
		File f = new File("D:\\TEMP\\AAA\\UnrealShare\\VertMesh");


		for(final File ff : f.listFiles()) {
			if(!ff.getName().endsWith("d.3d")){
				continue;
			}

			try {
				VertMesh vm = new VertMesh(ff);
				vm.read();

				// test VertMesh to .obj conversion
				final ObjStaticMesh objStaticMesh = new ObjStaticMesh(vm);
				final String baseName = FilenameUtils.getBaseName(ff.getName());
				final File mtlFile = new File("D:\\TEMP\\XXX\\"+baseName+".mtl");
				final File objFile = new File("D:\\TEMP\\XXX\\"+baseName+".obj");
				objStaticMesh.export(mtlFile, objFile);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
}
