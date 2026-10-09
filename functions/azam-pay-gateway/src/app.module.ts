import { Module } from "@nestjs/common";
import { ConfigModule } from "@nestjs/config";
import * as Joi from "joi";

const azamPayEnvironmentSchema = Joi.object({
  AZAMPAY_ENV: Joi.string().trim().required(),
  AZAMPAY_APP_NAME: Joi.string().trim().required(),
  AZAMPAY_CLIENT_ID: Joi.string().trim().required(),
  AZAMPAY_CLIENT_SECRET: Joi.string().trim().required(),
  AZAMPAY_API_KEY: Joi.string().trim().required(),
  AZAMPAY_ACCOUNT_NUMBER: Joi.string().trim().required(),
  AZAMPAY_CALLBACK_URL: Joi.string().uri({ scheme: ["http", "https"] }).required(),
  AZAMPAY_REDIRECT_SUCCESS_URL: Joi.string().uri({ scheme: ["http", "https"] }).required(),
  AZAMPAY_REDIRECT_FAIL_URL: Joi.string().uri({ scheme: ["http", "https"] }).required(),
});

@Module({
  imports: [
    ConfigModule.forRoot({
      isGlobal: true,
      validate: (config) => {
        const { error, value } = azamPayEnvironmentSchema.validate(config, {
          abortEarly: false,
          allowUnknown: true,
        });
        if (error) {
          throw new Error(error.details.map(({ message }) => message).join("; "));
        }
        return value;
      },
    }),
  ],
})
export class AppModule {}
