package mcjty.rftools.blocks.teleporter;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.IClientCommandHandler;
import mcjty.lib.network.PacketListFromServer;
import mcjty.lib.varia.Logging;
import mcjty.rftools.RFTools;
import mcjty.lib.typed.Type;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.List;

public class PacketReceiversReady extends PacketListFromServer<PacketReceiversReady,TeleportDestinationClientInfo> {

    public PacketReceiversReady() {
    }

    public PacketReceiversReady(BlockPos pos, String command, List<TeleportDestinationClientInfo> list) {
        super(pos, command, list);
    }

    public static class Handler implements IMessageHandler<PacketReceiversReady, IMessage> {
        @Override
        public IMessage onMessage(PacketReceiversReady message, MessageContext ctx) {
            RFTools.proxy.addScheduledTaskClient(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketReceiversReady message, MessageContext ctx) {
            TileEntity te = RFTools.proxy.getClientWorld().getTileEntity(message.pos);
            if(!(te instanceof IClientCommandHandler)) {
                Logging.log("createInventoryReadyPacket: TileEntity is not a ClientCommandHandler!");
                return;
            }
            IClientCommandHandler clientCommandHandler = (IClientCommandHandler) te;
            if (!clientCommandHandler.receiveListFromServer(message.command, message.list, Type.create(TeleportDestinationClientInfo.class))) {
                Logging.log("Command " + message.command + " was not handled!");
            }
        }
    }

    @Override
    protected TeleportDestinationClientInfo createItem(ByteBuf buf) {
        return new TeleportDestinationClientInfo(buf);
    }

    @Override
    protected void writeItemToBuf(ByteBuf buf, TeleportDestinationClientInfo item) {
        item.toBytes(buf);
    }
}
