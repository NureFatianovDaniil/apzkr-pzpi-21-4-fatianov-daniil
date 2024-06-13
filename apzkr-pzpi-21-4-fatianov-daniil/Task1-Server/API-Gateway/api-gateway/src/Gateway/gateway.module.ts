import { Module } from '@nestjs/common';
import { ConfigurationModule } from '../config.module';
import { GatewayController } from './gateway.controller';
import { GatewayService } from './gateway.service';

@Module({
    imports: [ConfigurationModule],
    controllers: [GatewayController],
    providers: [GatewayService],
})
export class GatewayModule {}