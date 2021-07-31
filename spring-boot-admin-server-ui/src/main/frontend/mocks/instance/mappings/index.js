import { rest } from "msw";
import { mappings } from "./data";

const mappingsEndpoint = [
  rest.get(
    "/instances/:instanceId/actuator/mappings",
    (req, res, ctx) => {
      return res(ctx.status(200), ctx.json(arbeitgeber));
    }
  ),
];

export default mappingsEndpoint;
