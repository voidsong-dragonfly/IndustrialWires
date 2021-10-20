package malte0811.industrialwires.client;

import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import malte0811.industrialwires.client.multiblock_io_model.SmartLightingQuadIW;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.vector.Vector3f;

@SideOnly(Side.CLIENT)
public class ClientUtilsIW {

    @SideOnly(Side.CLIENT)
    public static BakedQuad bakeQuad(RawQuad raw, Matrix4 transform, Matrix4 transfNormal) {
        VertexFormat format = DefaultVertexFormats.ITEM;
        UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(format);
        builder.setQuadOrientation(raw.facing);
        builder.setTexture(raw.tex);
        Vector3f[] vertices = raw.vertices;
        float[][] uvs = raw.uvs;
        Vector3f normal = transfNormal.apply(raw.normal);
        OBJModel.Normal faceNormal = new OBJModel.Normal(normal.x, normal.y, normal.z);
        for (int i = 0; i < 4; i++) {
            putVertexData(format, builder, transform.apply(vertices[i]), faceNormal, uvs[i][0], uvs[i][1], raw.tex,
                    raw.colorA);
        }
        BakedQuad ret = builder.build();
        if (raw.light>0) {
            ret = new SmartLightingQuadIW(ret, raw.light);
        }
        return ret;
    }

    //mostly copied from IE's ClientUtils, it has protected access there...
    @SideOnly(Side.CLIENT)
    public static void putVertexData(VertexFormat format, UnpackedBakedQuad.Builder builder, Vector3f pos, OBJModel.Normal faceNormal, double u, double v, TextureAtlasSprite sprite, float[] colorA) {
        for (int e = 0; e < format.getElementCount(); e++)
            switch (format.getElement(e).getUsage()) {
                case POSITION:
                    builder.put(e, pos.getX(), pos.getY(), pos.getZ(), 0);
                    break;
                case COLOR:
                    builder.put(e, colorA[0], colorA[1], colorA[2], colorA[3]);
                    break;
                case UV:
                    if (sprite == null)//Double Safety. I have no idea how it even happens, but it somehow did .-.
                        sprite = Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite();
                    builder.put(e,
                            sprite.getInterpolatedU(u),
                            sprite.getInterpolatedV((v)),
                            0, 1);
                    break;
                case NORMAL:
                    builder.put(e, faceNormal.x, faceNormal.y, faceNormal.z, 0);
                    break;
                default:
                    builder.put(e);
            }
    }

}