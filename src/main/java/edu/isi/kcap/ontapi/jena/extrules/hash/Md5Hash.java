package edu.isi.kcap.ontapi.jena.extrules.hash;

import org.apache.jena.reasoner.rulesys.*;
import org.apache.jena.reasoner.rulesys.builtins.BaseBuiltin;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.jena.graph.*;

/**
 * Bind the second arg to the md5 hash of first arg
 */
public class Md5Hash extends BaseBuiltin {

  /**
   * Return a name for this builtin, normally this will be the name of the
   * functor that will be used to invoke it.
   */
  public String getName() {
    return "md5hash";
  }

  /**
   * Return the expected number of arguments for this functor or 0 if the
   * number is flexible.
   */
  public int getArgLength() {
    return 2;
  }

  /**
   * This method is invoked when the builtin is called in a rule body.
   * 
   * @param args
   *            the array of argument values for the builtin, this is an array
   *            of Nodes, some of which may be Node_RuleVariables.
   * @param length
   *            the length of the argument list, may be less than the length
   *            of the args array for some rule engines
   * @param context
   *            an execution context giving access to other relevant data
   * @return return true if the buildin predicate is deemed to have succeeded
   *         in the current environment
   */
  public boolean bodyCall(Node[] args, int length, RuleContext context) {
    checkArgs(length, context);
    BindingEnvironment env = context.getEnv();
    Node n1 = getArg(0, args, context);
    Node n2 = getArg(1, args, context);
    if (n1.isLiteral()) {
      Object v1 = n1.getLiteralValue();
      if (v1 instanceof String) {
        String sv1 = (String) v1;
        String hash = DigestUtils.md5Hex(sv1);
        return env.bind(n2, NodeFactory.createLiteral(hash));
      }
    }
    return false;
  }

}
