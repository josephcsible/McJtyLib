package mcjty.lib.network;

import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import mcjty.lib.varia.Logging;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;

/**
 * This is a packet that can be used to send a command from the client side (typically the GUI) to
 * a tile entity on the server side that implements CommandHandler. This will call 'execute()' on
 * that command handler.
 */
public class PacketServerCommand extends AbstractServerCommand implements IMessageHandler<PacketServerCommand, IMessage> {

    public PacketServerCommand() {
    }

    public PacketServerCommand(BlockPos pos, String command, Argument... arguments) {
        super(pos, command, arguments);
    }

    public PacketServerCommand(int x, int y, int z, String command, Argument... arguments) {
        super(x, y, z, command, arguments);
    }

    @Override
    public IMessage onMessage(PacketServerCommand message, MessageContext ctx) {
        EntityPlayerMP playerEntity = ctx.getServerHandler().playerEntity;
        TileEntity te = playerEntity.worldObj.getTileEntity(new BlockPos(message.x, message.y, message.z));
        if(!(te instanceof CommandHandler)) {
            Logging.log("createStartScanPacket: TileEntity is not a CommandHandler!");
            return null;
        }
        CommandHandler commandHandler = (CommandHandler) te;
        if (!commandHandler.execute(playerEntity, message.command, message.args)) {
            Logging.log("Command " + message.command + " was not handled!");
        }
        return null;
    }

}
