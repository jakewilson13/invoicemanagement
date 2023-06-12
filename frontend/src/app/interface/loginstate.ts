import { DataState } from "../enum/datastate.enum";

export interface LoginState {
  dataState: DataState;
  loginSuccess?: boolean; //login state will have  a property of success if the login was successful
  error?: string; //loginState will have a property of error if an error occured
  message?: string;
  isUsingMfa?: boolean;
  phone?: string;
}
