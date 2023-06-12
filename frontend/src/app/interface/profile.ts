import { User } from "./user";
import {Events} from "./events";
import {Role} from "./role";

export interface Profile {
  user: User;
  events?: Events[];
  roles?: Role[];
  access_token?: string;
  refresh_token?: string;
}
